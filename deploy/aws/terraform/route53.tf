# Optional public DNS record for the application's HTTPS hostname.
# The existing hosted zone and ACM certificate are managed outside this module.
# lb.tf supplies the ALB; variables.tf controls whether Terraform manages DNS.

# Create an A alias from domain_name to the ALB when hosted_zone_id is non-null.
# With null, no record is managed; publish equivalent DNS at your provider using
# outputs.tf's alb_dns_name. The hostname must match the supplied ACM certificate.
# The alias target uses the ALB's own zone_id, not the public hosted zone where the
# record is created; these two zone IDs have different purposes.
# evaluate_target_health inherits ELB health for Route 53 routing decisions. It
# does not add another host, create a separate HTTP probe, or provide failover alone.
# Removing hosted_zone_id destroys the managed record; plan DNS migration before
# that change. This file neither registers a domain nor creates validation records.
# Reference: https://docs.aws.amazon.com/Route53/latest/APIReference/API_AliasTarget.html
resource "aws_route53_record" "application" {
  count   = var.hosted_zone_id == null ? 0 : 1
  zone_id = var.hosted_zone_id
  name    = var.domain_name
  type    = "A"
  alias {
    name                   = aws_lb.application.dns_name
    zone_id                = aws_lb.application.zone_id
    evaluate_target_health = true
  }
}
