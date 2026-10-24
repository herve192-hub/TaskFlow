# Private image repositories shared by the publisher and the application host.
# locals.tf defines repository keys; outputs.tf returns the URL map expected by
# ../taskflow.py. Actual images are built and pushed outside Terraform.

# Create one repository for each of the eight services, including frontend.
# The publisher pushes a unique release tag and records its digest in an S3 release
# manifest. The host pulls those digests using iam.tf; it has no push permission.
# IMMUTABLE prevents overwriting an existing tag, so retries need a fresh release ID.
# Scan-on-push generates findings; it does not block installation automatically.
# No lifecycle policy expires images here, preserving old digests for rollback.
# force_delete=false prevents forced removal of nonempty repositories.
# Renaming a for_each service key changes its Terraform address; changing var.name
# changes repository names. Coordinate those edits with locals.tf, Compose, the
# helper's service validation, and release manifests. AES256 is ECR encryption,
# independent of the customer-managed S3 release key in kms.tf.
resource "aws_ecr_repository" "application" {
  for_each             = local.services
  name                 = "${var.name}/${each.key}"
  image_tag_mutability = "IMMUTABLE"
  force_delete         = false
  image_scanning_configuration { scan_on_push = true }
  encryption_configuration { encryption_type = "AES256" }
}
