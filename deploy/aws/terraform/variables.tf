# Operator inputs to this root module; supply deployment-specific values through
# the ignored terraform.tfvars copied from settings.example or another variable
# source. Defaults describe this production design, not measured capacity.
# Inputs contain infrastructure identifiers and secret ARNs, never secret values.
# Only the validations shown below run: successful validation does not verify
# provider-side routes, certificate issuance, database permissions, or backups.

# AWS region used by providers.tf and exported for AWS CLI/Docker log delivery.
# Keep regional subnets, ALB, ACM certificate, secret ARNs, and KMS keys consistent.
# Changing it affects regional infrastructure; review as an infrastructure move.
variable "region" {
  type    = string
  default = "us-east-1"
}

# Stack name/prefix used for ECR paths, the release bucket prefix, IAM identities,
# ALB/target-group names, alarms, and the log group. The short format fits the
# shared AWS naming constraints. Changing it can replace named resources; it is
# not a harmless display-label edit. Default provider tags still say production.
variable "name" {
  type    = string
  default = "taskflow-production"
  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{2,23}$", var.name))
    error_message = "Use 3–24 lowercase letters, digits, or hyphens, starting with a letter."
  }
}

# Existing network boundary used by security.tf and lb.tf's target group.
# ec2.tf verifies supplied subnets belong to it. This module creates no VPC,
# Internet Gateway, route tables, DNS configuration, or private data-service link.
variable "vpc_id" {
  type        = string
  description = "Existing VPC with DNS resolution and an Internet Gateway."
}

# Ordered public subnets for the ALB; the first also hosts the EC2 instance.
# At least two distinct IDs are required here, and ec2.tf checks their AZ spread.
# Reordering can move/replace the host. Supply actual ALB-compatible public subnet
# CIDRs (/16 or narrower is required by the release helper) with IPv4 routes.
# outputs.tf exports these CIDRs as the NGINX trust list; refresh release settings
# after subnet changes, not just the ALB resource.
variable "public_subnet_ids" {
  type        = list(string)
  description = "At least two public subnets in different AZs. EC2 runs in the first; all must route to the Internet Gateway."
  validation {
    condition     = length(distinct(var.public_subnet_ids)) >= 2
    error_message = "Provide at least two distinct public subnets."
  }
}

# Exact browser hostname used for the optional DNS record and HTTPS origin output.
# Supply only a lowercase DNS name, without scheme, port, path, or trailing slash.
# The supplied ACM certificate must cover it; the regex is not a DNS lookup or a
# certificate-coverage check. No certificate or registered domain is created.
variable "domain_name" {
  type        = string
  description = "Application hostname covered by the existing ACM certificate."
  validation {
    condition     = can(regex("^[a-z0-9][a-z0-9.-]+\\.[a-z]{2,}$", var.domain_name))
    error_message = "Use a DNS hostname, without scheme, port, or path."
  }
}

# Explicit IPv4 destination CIDRs and TCP ports for external MongoDB/Kafka.
# security.tf creates one outbound rule per list entry, including all advertised
# brokers. Correct routes, provider allowlists, TLS, and authentication are separate.
# The port/CIDR checks below reject unrestricted IPv4 egress and invalid port values.
# List indexes become resource keys, so reordering entries can replace rules.
# HTTPS for AWS APIs/bootstrap is allowed by a separate rule in security.tf.
variable "data_egress_rules" {
  type = list(object({
    cidr = string
    port = number
  }))
  description = "All MongoDB/Kafka endpoint IPv4 CIDRs and TLS TCP ports, including advertised brokers. HTTPS for AWS APIs/bootstrap is separate."
  validation {
    condition = length(var.data_egress_rules) > 0 && alltrue([
      for rule in var.data_egress_rules : can(cidrnetmask(rule.cidr)) && rule.cidr != "0.0.0.0/0" && rule.port >= 1024 && rule.port <= 65535 && floor(rule.port) == rule.port
    ])
    error_message = "Provide explicit IPv4 database/broker CIDRs and integer TCP ports 1024–65535; unrestricted CIDRs are forbidden."
  }
}

# Existing issued ACM certificate used by lb.tf's HTTPS listener in this region.
# Its SANs must cover domain_name, and its validation records/renewal prerequisites
# must be maintained externally. It is not a certificate file or TLS private key.
variable "certificate_arn" {
  type        = string
  description = "Issued ACM certificate in this region covering domain_name."
}

# Existing public Route 53 zone in which route53.tf may create the A alias.
# null delegates DNS management to your external provider. This ID differs from
# the ALB's own hosted-zone ID used on the alias target.
# Changing non-null to null removes the managed record; coordinate DNS migration.
variable "hosted_zone_id" {
  type        = string
  default     = null
  description = "Optional Route 53 public zone ID; otherwise configure DNS with your provider."
}

# Existing Secrets Manager JSON secret following the schema in ../README.md.
# locals.tf/iam.tf authorize reads; ../taskflow.py fetches values on EC2 and writes
# service-scoped secret files. Terraform does not read those values or perform
# rotation. Keep the ARN/secret in the deployment region; redeploy after rotation.
variable "application_secret_arn" {
  type        = string
  description = "Existing Secrets Manager JSON secret; values are fetched on the host, never through Terraform."
}

# Optional separate first-administrator JSON secret, never part of the main bundle.
# Used only by ../taskflow.py deploy --bootstrap and authorized through local.secrets.
# After successful provisioning, remove this input and apply to revoke future reads.
# IAM changes cannot erase copies already downloaded; the helper removes successful
# bootstrap mounts/files, and failed attempts need inspection before cleanup.
variable "bootstrap_secret_arn" {
  type        = string
  default     = null
  description = "Optional separate first-admin secret. Remove after initial provisioning."
}

# Existing customer-managed keys encrypting the supplied Secrets Manager secrets.
# iam.tf adds regional Secrets Manager-mediated decrypt rights for these key ARNs.
# Their key policies must allow the role too. Leave empty for secrets that do not
# need such grants. kms.tf creates a different key for S3 release archives.
variable "secret_kms_key_arns" {
  type        = list(string)
  default     = []
  description = "Customer-managed KMS keys for these secrets, if used. Key policies must also permit this role."
}

# EC2 compute/memory sizing for the x86_64 AL2023 image and all Compose containers.
# The default t3.large supplies the 8 GiB baseline; this type is not auto-selected
# from load and no variable validation enforces RAM/architecture. Check both before
# overriding, and size CPU credits, memory, storage, and traffic from measurement.
variable "instance_type" {
  type        = string
  default     = "t3.large"
  description = "x86_64 instance with at least 8 GiB RAM; size further from measured traffic."
}

# Optional reviewed AL2023 x86_64 AMI for ec2.tf instead of the moving SSM default.
# Choose an image compatible with user-data.sh and the selected instance type.
# Changing the selected AMI can replace the host; it is not an in-place OS patch.
# The public SSM data lookup remains defined even with this override.
variable "ami_id" {
  type        = string
  default     = null
  description = "Optional pinned Amazon Linux 2023 x86_64 AMI; defaults to the current public SSM image parameter."
}

# Controls ALB deletion protection and EC2 API termination protection together.
# Disable deliberately before a reviewed teardown or a replacement that protection
# would block. This flag does not protect database backups or make S3/ECR empty:
# their separate force_destroy/force_delete=false guards remain in effect.
variable "protect_resources" {
  type        = bool
  default     = true
  description = "Enable ALB deletion and EC2 termination protection. Disable explicitly for reviewed teardown/replacement."
}

# Existing SNS destinations used by both alarms in cloudwatch.tf.
# Provide compatible topics with working permissions/subscriptions and test delivery.
# An empty list creates alarms with no notification actions; this module creates
# neither the SNS topics/subscribers nor automated recovery actions.
variable "alarm_topic_arns" {
  type        = list(string)
  default     = []
  description = "Existing SNS topics for CloudWatch alarms. Empty means alarms have no notification destination."
}
