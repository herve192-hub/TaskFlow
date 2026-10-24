# Deployment handoff from Terraform to the publisher and operations workflow.
# Export with: terraform output -json deployment > /tmp/taskflow-deployment.json
# Pass that object to ../taskflow.py publish --settings. Its release manifest carries
# the runtime settings to EC2; the deploy subcommand uses them when rendering Compose.
# These are public settings/ARNs, not decrypted values, but still describe your
# infrastructure. Do not commit generated deployment.json or edit field names
# without updating the helper's settings validation and documentation.

# One JSON object preserves the contract expected by ../taskflow.py.
# Some fields are runtime requirements; others support IAM setup, SSM access, DNS,
# and provider allowlists. It is not proof that cloud-init or an app release finished.
output "deployment" {
  description = "Public runtime settings; pass these to the host deployment helper."
  value = {
    # AWS region used for release API calls and the Compose awslogs driver.
    region = var.region
    # Exact HTTPS browser origin consumed by Spring's production/cookie/CORS checks.
    origin = "https://${var.domain_name}"
    # Main secret identifier; values are fetched only on the deployment host.
    application_secret_arn = var.application_secret_arn
    # Optional first-admin identifier; deploy reads it only with --bootstrap.
    bootstrap_secret_arn = var.bootstrap_secret_arn
    # Existing group to which Docker writes logs; the helper sets TASKFLOW_LOG_GROUP.
    log_group = aws_cloudwatch_log_group.application.name
    # Trusted proxy CIDRs written into NGINX's alb-trust.conf on deployment.
    alb_subnet_cidrs = [for id in var.public_subnet_ids : data.aws_subnet.public[id].cidr_block]
    # Service-key -> repository URL map used for publishing and digest validation.
    ecr_repositories = { for name, repository in aws_ecr_repository.application : name => repository.repository_url }
    # Actual generated bucket name for releases/<release-id>.tgz upload/download.
    release_bucket = aws_s3_bucket.releases.id
    # Operator/publisher policy input for encrypted uploads; not a secret value.
    release_kms_key_arn = aws_kms_key.releases.arn
    # SSM session target and operational identity of the current application host.
    instance_id = aws_instance.host.id
    # Stable outbound address for MongoDB/Kafka provider-side IP allowlists.
    egress_ip = aws_eip.host.public_ip
    # DNS target for operators managing records outside the optional Route 53 resource.
    alb_dns_name = aws_lb.application.dns_name
  }
}
