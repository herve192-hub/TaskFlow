# Read existing AWS metadata without creating infrastructure.
# ec2.tf consumes these facts, iam.tf builds partition-aware ARNs, and outputs.tf
# exports the ALB subnet CIDRs. These lookups need plan-time AWS read permissions.
# VPCs, subnets, routes, and the public AMI parameter are not owned by this module.

# Discover the active AWS partition and its DNS suffix from provider context.
# iam.tf uses them for the managed SSM policy ARN and regional KMS ViaService
# conditions, avoiding hardcoded commercial-partition endpoints.
# This lookup does not select an account or authenticate the provider.
data "aws_partition" "current" {}

# Read each supplied subnet once, keyed by subnet ID for stable references.
# ec2.tf checks that they belong to vpc_id and span at least two AZs; lb.tf uses
# the original ordered input list, and ec2.tf launches in that list's first subnet.
# outputs.tf passes their CIDRs to the host helper for NGINX's trusted-ALB rules.
# The name "public" expresses the required network design, not an AWS check that
# an Internet Gateway route, permissive NACL, or sufficient free addresses exists.
data "aws_subnet" "public" {
  for_each = toset(var.public_subnet_ids)
  id       = each.value
}

# Read AWS's public parameter containing the current AL2023 x86_64 AMI ID.
# ec2.tf selects it when no nonempty ami_id override is supplied; the lookup
# itself still exists and is read even when an override is supplied.
# The parameter can advance between plans and trigger host replacement. Pin a
# reviewed ami_id for controlled rollouts. This public value is not an app secret.
data "aws_ssm_parameter" "al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}
