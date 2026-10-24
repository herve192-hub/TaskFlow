# Customer-managed encryption for release archives in S3.
# This key is separate from ECR encryption, EC2 root-volume encryption, log storage,
# and externally supplied Secrets Manager keys.

# Create the symmetric key selected by the S3 default-encryption configuration.
# s3.tf enables SSE-KMS and bucket keys. iam.tf gives the host S3-mediated decrypt
# permission; the external publisher needs GenerateDataKey for uploads.
# outputs.tf exposes this ARN for publisher/operator policy configuration.
# No explicit policy is supplied: KMS's default policy enables account IAM
# delegation. Coordinate changes to that policy with the identity policies.
# Rotation renews key material, not JWT signing keys or application secrets.
# Destroy schedules deletion after 30 days; retained S3 versions still need this
# key, and become unreadable if it is deleted.
# Reference: https://docs.aws.amazon.com/kms/latest/developerguide/key-policies.html
resource "aws_kms_key" "releases" {
  description             = "${var.name} release archive encryption"
  enable_key_rotation     = true
  deletion_window_in_days = 30
}
