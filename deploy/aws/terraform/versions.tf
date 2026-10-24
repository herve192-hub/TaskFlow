# Terraform engine compatibility and AWS provider source/version constraints.
# Run init before validate/plan so Terraform installs the required provider.

# Accept Terraform 1.10 through the 1.x series and AWS provider 6.x.
# The constraint is an allowed range; .terraform.lock.hcl selects the exact
# reviewed provider build/checksums used by ordinary init and CI.
# Use init -upgrade only for an intentional provider update and review the lock
# diff plus plan. The CI workflow pins its Terraform executable independently.
# No backend is declared: state is local unless operators configure a backend.
# .gitignore prevents publishing local state but does not encrypt or back it up;
# choose protected remote state with locking before shared operation.
terraform {
  required_version = ">= 1.10, < 2.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}
