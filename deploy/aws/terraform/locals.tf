# Shared expressions used across resource groups; locals create no AWS objects.
# Keep these derived values here so resource files share the same naming/access
# contracts instead of maintaining divergent copies.

locals {
  # Stable service identifiers used as ECR for_each keys in ecr.tf.
  # outputs.tf exposes the same keys to ../taskflow.py. Adding/renaming one requires
  # coordinating its DOCKERFILES/validation map, Compose services, and release tests;
  # editing this set alone does not implement a new application service.
  services = toset(["frontend", "discovery-server", "config-server", "api-gateway", "auth-service", "user-service", "project-service", "task-service"])
  # Allowlist of existing Secrets Manager ARNs for iam.tf's GetSecretValue grant.
  # Include the separate first-admin secret only when bootstrap_secret_arn is set.
  # Remove that input and apply after provisioning to revoke future bootstrap reads.
  # This expression contains identifiers, never decrypted secret values.
  secrets = concat([var.application_secret_arn], var.bootstrap_secret_arn == null ? [] : [var.bootstrap_secret_arn])
}
