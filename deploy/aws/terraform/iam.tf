# Permissions for the EC2 host, not for Terraform operators or release publishers.
# The host pulls images, downloads archives, reads specific runtime secrets, and
# writes logs. Temporary instance credentials avoid embedded access keys.
# Network access is separate: an IAM allow cannot bypass security.tf's rules.

# Identity assumed by the EC2 service through the instance profile below.
# The trust policy controls WHO may assume this role; the attached SSM and inline
# runtime policies control WHAT its credentials may do.
# The role itself neither attaches to a host nor authorizes a human SSM operator.
# ec2.tf references the profile; host-side AWS CLI and Docker use role credentials.
resource "aws_iam_role" "host" {
  name = "${var.name}-host"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "ec2.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

# Attach AWS's managed core Systems Manager permissions to the host role.
# user-data.sh starts the SSM agent and security.tf permits outbound HTTPS, enabling
# administration without opening SSH. Operators need their own session permissions
# and CLI/console access; this attachment grants the agent its side of that contract.
# data.tf supplies the ARN partition instead of assuming arn:aws.
# AWS maintains the policy, which includes general SSM management permissions in
# addition to this module's narrower custom runtime permissions.
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.host.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Permit host-side deployment and logging against this stack's named resources.
# References link the policy to ecr.tf, s3.tf, kms.tf, and cloudwatch.tf.
# local.secrets supplies existing secret ARNs. ../taskflow.py reads their values on
# the host and distributes files to the appropriate Compose services; Terraform
# does not fetch or store those values.
# There are no ECR push, S3 write, secret-update, or log-group-create grants here.
# KMS permissions also require compatible key policies; an IAM allow cannot override
# a key policy that denies this role or does not permit IAM delegation.
resource "aws_iam_role_policy" "runtime" {
  role = aws_iam_role.host.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = concat([
      # Token retrieval has no repository-level resource scope. The next statement
      # limits actual image/layer reads to the repositories created in ecr.tf.
      { Effect = "Allow", Action = ["ecr:GetAuthorizationToken"], Resource = "*" },
      {
        Effect   = "Allow"
        Action   = ["ecr:BatchGetImage", "ecr:GetDownloadUrlForLayer", "ecr:BatchCheckLayerAvailability"]
        Resource = [for repository in aws_ecr_repository.application : repository.arn]
      },
      # Removing bootstrap_secret_arn revokes future reads after apply; it cannot
      # erase files already copied to a host. The helper removes bootstrap mounts
      # and files after successful initial administrator provisioning.
      { Effect = "Allow", Action = ["secretsmanager:GetSecretValue"], Resource = local.secrets },
      # Allow known release-object downloads only, without bucket listing or uploads.
      # The release bucket is not used as Terraform state storage.
      { Effect = "Allow", Action = ["s3:GetObject"], Resource = "${aws_s3_bucket.releases.arn}/releases/*" },
      {
        Effect = "Allow", Action = ["kms:Decrypt"], Resource = aws_kms_key.releases.arn
        # Limit use of this release key to S3-mediated decryption in this region.
        # s3.tf selects the key; the caller also needs the S3 object-read grant above.
        Condition = { StringEquals = { "kms:ViaService" = "s3.${var.region}.${data.aws_partition.current.dns_suffix}" } }
      },
      {
        Effect = "Allow"
        # Docker creates container log streams and appends events inside the group
        # already provisioned in cloudwatch.tf; group creation/read access is separate.
        Action   = ["logs:CreateLogStream", "logs:PutLogEvents"]
        Resource = "${aws_cloudwatch_log_group.application.arn}:*"
      }
      # Optionally permit decrypting supplied Secrets Manager secrets with their
      # existing customer-managed keys. These are separate from kms.tf's release
      # key, and their key policies must permit use through regional Secrets Manager.
      ], length(var.secret_kms_key_arns) == 0 ? [] : [{
        Effect    = "Allow", Action = ["kms:Decrypt"], Resource = var.secret_kms_key_arns
        Condition = { StringEquals = { "kms:ViaService" = "secretsmanager.${var.region}.${data.aws_partition.current.dns_suffix}" } }
    }])
  })
}

# Attach the host role to EC2 through the mechanism EC2 expects: an instance profile.
# ec2.tf uses this profile's name, and instance metadata supplies temporary role
# credentials to host-side tools. The profile does not create static access keys.
# Application containers use scoped local files, while the CLI, SSM agent, and
# Docker logging daemon use the instance's AWS identity.
resource "aws_iam_instance_profile" "host" {
  name = "${var.name}-host"
  role = aws_iam_role.host.name
}
