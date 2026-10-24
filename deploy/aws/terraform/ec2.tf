# One Docker host and its stable outbound IPv4 address.
# The host runs the complete production Compose stack. Multiple ALB subnets do not
# make the application highly available: host replacement and releases can interrupt
# service. Business data stays in separately operated MongoDB/Kafka services.

# Launch the Amazon Linux 2023 x86_64 host for all application containers.
# data.tf provides the default AMI and subnet facts; variables.tf accepts a pinned
# AMI and instance size. security.tf controls traffic and iam.tf supplies its role.
# lb.tf registers this instance as the only HTTP application target.
# user-data.sh installs host tools at first boot. ../taskflow.py later downloads
# and installs a release through SSM; instance creation alone does not deploy it.
# Keep releases in S3 for rebuilding the host and treat its root disk as local
# runtime storage rather than a backup of application data.
resource "aws_instance" "host" {
  # Pin ami_id for reviewed host updates: the default SSM parameter can advance
  # between plans. The instance type must support x86_64 and the Compose memory
  # limits (8 GiB baseline); this file does not enforce available RAM.
  ami           = coalesce(var.ami_id, data.aws_ssm_parameter.al2023.value)
  instance_type = var.instance_type
  # The first supplied public subnet determines this host's only AZ. Its initial
  # public IP permits package downloads before the Elastic IP is associated.
  # Only ALB ingress is allowed by security.tf; there is no SSH ingress/key pair.
  subnet_id                   = var.public_subnet_ids[0]
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.host.id]
  iam_instance_profile        = aws_iam_instance_profile.host.name
  # API termination protection must be disabled deliberately for replacement or
  # teardown. It is neither a backup nor an automatic recovery mechanism.
  disable_api_termination = var.protect_resources
  # file() sends every byte of user-data.sh, including comments, as EC2 user data.
  # With user_data_replace_on_change enabled, even documenting that script can
  # cause a future plan to replace a deployed host. User data is recorded in
  # Terraform/EC2 metadata, so never put secret values in the script.
  user_data                   = file("${path.module}/user-data.sh")
  user_data_replace_on_change = true
  # Enable EC2 detailed monitoring. This does not install guest memory/disk metrics;
  # cloudwatch.tf's host alarm uses EC2's status-check metric.
  monitoring = true
  # Require IMDSv2 tokens and limit metadata response hops through container
  # networks. Host-side AWS CLI/Docker use the instance role; application
  # containers receive scoped secret files instead of AWS credentials.
  metadata_options {
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
    instance_metadata_tags      = "disabled"
  }
  # Encrypted gp3 storage holds the OS, image layers, releases, and local secrets.
  # 80 GiB is a starting size, not autoscaling. Monitor capacity and retain S3
  # archives so host-local state can be reconstructed after replacement.
  root_block_device {
    volume_type = "gp3"
    volume_size = 80
    encrypted   = true
  }
  tags = { Name = var.name }
  # Bootstrap requires ready outbound HTTPS and SSM/runtime permissions. These
  # policy/rule dependencies are not all implied by the instance-profile reference.
  depends_on = [aws_vpc_security_group_egress_rule.host_https, aws_iam_role_policy_attachment.ssm, aws_iam_role_policy.runtime]
  lifecycle {
    # Check subnet VPC membership and ALB AZ spread using data.tf. This does not
    # validate routes, NACLs, free IP addresses, or an Internet Gateway attachment.
    precondition {
      condition     = alltrue([for subnet in data.aws_subnet.public : subnet.vpc_id == var.vpc_id]) && length(toset([for subnet in data.aws_subnet.public : subnet.availability_zone])) >= 2
      error_message = "Public subnets must belong to this VPC and span at least two AZs."
    }
  }
}

# Allocate a VPC Elastic IP and associate it with the application instance.
# External MongoDB/Kafka providers can allowlist outputs.tf's egress_ip instead of
# a transient public address. Their routing and firewalls are managed externally.
# The EIP does not open inbound ports or provide NAT for other instances.
# Retaining this resource lets Terraform associate the same address after a host
# replacement. Destroying it releases the address and invalidates such allowlists.
# The host starts with a temporary public IP before this association is ready.
resource "aws_eip" "host" {
  domain   = "vpc"
  instance = aws_instance.host.id
  tags     = { Name = "${var.name}-egress" }
}
