# AWS connection settings shared by every resource and data source in this module.
# Terraform loads all root .tf files together; filenames do not determine creation
# order. References/depends_on establish dependencies across the service files.

# Use the operator's normal AWS credential chain (for example an authenticated
# AWS_PROFILE/SSO session) and var.region; no access keys or profile are embedded.
# Regional resources use that region; IAM resources are account-global.
# There is no account-ID allowlist here: verify the authenticated account before
# planning/applying. EC2's runtime role in iam.tf is separate from this operator.
# Default tags identify supported resources for inventory/cost allocation; they
# do not implement authorization or change application runtime configuration.
provider "aws" {
  region = var.region
  default_tags {
    tags = { Application = "TaskFlow", Environment = "production", ManagedBy = "Terraform" }
  }
}
