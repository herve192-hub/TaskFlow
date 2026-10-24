# Private, versioned storage for deployable TaskFlow release archives.
# ../taskflow.py uploads releases/<release-id>.tgz from the build machine. The host
# downloads a known object using iam.tf and extracts its Compose/config files.
# Container images remain in ECR (ecr.tf); secret values remain in Secrets Manager.
# This bucket is neither public website hosting nor a Terraform state backend.

# Create the bucket holding release archives and their manifests.
# bucket_prefix lets Terraform generate a unique suffix; outputs.tf returns the
# actual name, so publishers must use release_bucket instead of guessing it.
# Archives include digest-pinned image references and public deployment settings,
# not database passwords or signing keys. Access still remains private.
# Protection, versioning, encryption, and transport policy are separate resources
# below; creating this bucket alone does not configure those controls.
# force_destroy=false requires deliberate removal of objects/versions before
# teardown. There is no expiry/replication policy; plan storage cost and retention.
resource "aws_s3_bucket" "releases" {
  bucket_prefix = "${var.name}-releases-"
  force_destroy = false
}

# Block public ACLs and bucket policies from exposing the release store.
# The four switches reject new public grants and ignore/restrict existing ones.
# They attach to the bucket above and complement the TLS-only policy below.
# They do not grant the publisher/host access, replace IAM, or disable access by
# authorized principals. iam.tf supplies only the host's required object reads.
resource "aws_s3_bucket_public_access_block" "releases" {
  bucket                  = aws_s3_bucket.releases.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Keep older object versions when a release archive is overwritten or deleted.
# Versioning supports recovery from accidental object changes, but does not make
# keys immutable or enable Object Lock. ECR tag immutability is a different control.
# The normal download uses the current version; restoring a previous S3 version is
# an explicit operator action, separate from reinstalling an older release ID.
# No lifecycle rule removes noncurrent versions, so they continue consuming storage.
# Deleting current objects alone will not empty a versioned bucket for teardown.
resource "aws_s3_bucket_versioning" "releases" {
  bucket = aws_s3_bucket.releases.id
  versioning_configuration { status = "Enabled" }
}

# Set SSE-KMS encryption for new release objects using kms.tf's managed key.
# Bucket keys reduce KMS request volume; they do not change the need for IAM/key
# permissions. The host's S3-mediated decrypt grant is in iam.tf, and the publisher
# needs upload access plus kms:GenerateDataKey on the key exported by outputs.tf.
# This sets the upload default; it does not re-encrypt old object versions or deny
# an otherwise authorized upload specifying a different encryption configuration.
# It encrypts archive contents at rest, not application traffic or external data.
resource "aws_s3_bucket_server_side_encryption_configuration" "releases" {
  bucket = aws_s3_bucket.releases.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.releases.arn
    }
    bucket_key_enabled = true
  }
}

# Deny non-TLS S3 requests for both the bucket itself and every object.
# Both ARNs are included because bucket-level and object-level APIs use different
# resources. An explicit deny also applies to principals with identity-based allows.
# This policy grants nobody read/write access; IAM still determines authorized
# publisher and host operations, while public-access blocking stays separate.
# It protects transport to S3, not certificate renewal at the ALB, object retention,
# or the presence/absence of sensitive material inside an uploaded archive.
resource "aws_s3_bucket_policy" "releases" {
  bucket = aws_s3_bucket.releases.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Deny", Principal = "*", Action = "s3:*"
      Resource  = [aws_s3_bucket.releases.arn, "${aws_s3_bucket.releases.arn}/*"]
      Condition = { Bool = { "aws:SecureTransport" = "false" } }
    }]
  })
}
