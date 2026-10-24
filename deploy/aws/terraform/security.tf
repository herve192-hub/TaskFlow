# Network boundaries for the public ALB and its single application host.
# Browser path: Internet -> ALB 80/443 -> host 8080 -> Docker NGINX/gateway.
# Administration and AWS API access use outbound HTTPS, not inbound SSH.
# Security groups are stateful: allowed connections admit their return traffic.
# Standalone rules keep ingress and egress explicit; avoid adding parallel inline
# rules to these groups. VPC routes, NACLs, DNS setup, and provider firewalls are
# external prerequisites and can still prevent traffic these groups permit.

# Create the security-group identity attached to aws_lb.application in lb.tf.
# Rules below admit public HTTPS/HTTP and allow only host-directed TCP 8080 egress.
# A group is the container for rules, not a listener or a route-table entry.
# Its VPC must match all supplied ALB subnets. name_prefix permits a unique suffix;
# other resources refer to its ID instead of depending on that generated name.
resource "aws_security_group" "alb" {
  name_prefix = "${var.name}-alb-"
  description = "Public HTTPS and HTTP redirect only"
  vpc_id      = var.vpc_id
}

# Create the group attached to aws_instance.host in ec2.tf.
# Only the ALB group's identity may initiate inbound application traffic on 8080.
# There are no SSH, database-listener, backend-service, or direct public web rules.
# Outbound HTTPS enables host AWS/tooling access; data_egress_rules enable the
# separately operated databases/brokers. This is a network boundary, not TLS or IAM.
# Keep the group aligned with lb.tf and ../compose.yml when changing published ports.
resource "aws_security_group" "host" {
  name_prefix = "${var.name}-host-"
  description = "Application ingress from ALB only; administration through SSM"
  vpc_id      = var.vpc_id
}

# Allow any IPv4 browser to reach the ALB's HTTPS listener on TCP 443.
# This is the intended public entry point, not permission to reach EC2 directly.
# lb.tf's https listener owns the ACM certificate and TLS policy; opening a group
# port does not create a listener or validate a certificate.
# IPv6 is not admitted here; enabling it needs coordinated ALB, DNS, and rule edits.
resource "aws_vpc_security_group_ingress_rule" "https" {
  security_group_id = aws_security_group.alb.id
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_ipv4         = "0.0.0.0/0"
}

# Allow public IPv4 TCP 80 only so lb.tf's HTTP listener can redirect to HTTPS.
# The ALB listener performs the redirect; the security-group rule itself cannot.
# No matching public EC2 port-80 permission exists. Remove/change both the rule
# and listener together if redirect behavior is intentionally changed.
resource "aws_vpc_security_group_ingress_rule" "http_redirect" {
  security_group_id = aws_security_group.alb.id
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
  cidr_ipv4         = "0.0.0.0/0"
}

# Allow ALB-initiated TCP 8080 traffic only to interfaces using the host group.
# This is the outbound half of the path used for both HTTP requests and /healthz
# checks. host_from_alb below supplies the inbound half; lb.tf sets target port 8080.
# Using the destination group follows host replacement without a fixed private IP.
# It does not allow arbitrary ALB egress to the Internet or backend service ports.
resource "aws_vpc_security_group_egress_rule" "alb_to_host" {
  security_group_id            = aws_security_group.alb.id
  referenced_security_group_id = aws_security_group.host.id
  ip_protocol                  = "tcp"
  from_port                    = 8080
  to_port                      = 8080
}

# Allow host TCP 8080 only from interfaces associated with the ALB group.
# Pairs with alb_to_host above and ../compose.yml's only published application port.
# This boundary is why ../nginx.conf can trust ALB-provided forwarding headers;
# keep it intact when changing load-balancer or host security-group attachments.
# The host has a public Elastic IP for egress, but this rule does not allow an
# Internet caller to bypass ALB TLS by connecting to that address directly.
resource "aws_vpc_security_group_ingress_rule" "host_from_alb" {
  security_group_id            = aws_security_group.host.id
  referenced_security_group_id = aws_security_group.alb.id
  ip_protocol                  = "tcp"
  from_port                    = 8080
  to_port                      = 8080
}

# Allow host TCP 443 to public IPv4 HTTPS endpoints.
# Needed for package/Compose bootstrap, ECR authentication/layers, S3 release
# downloads, Secrets Manager, CloudWatch, and the SSM agent. These destinations
# do not share one static CIDR list; HTTPS remains broader than data-service rules.
# This rule filters IP/port, not hostname, TLS identity, or the AWS API operation;
# TLS verification and IAM enforce those separate layers.
# The Trivy exception below is scoped to this intentionally public HTTPS egress.
// Public HTTPS is required for AWS APIs, ECR/S3 images, and checksum-verified
// bootstrap downloads. Other ports are restricted to database/broker CIDRs.
//trivy:ignore:AWS-0104
resource "aws_vpc_security_group_egress_rule" "host_https" {
  security_group_id = aws_security_group.host.id
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_ipv4         = "0.0.0.0/0"
}

# Create one outbound TCP rule for each configured MongoDB/Kafka CIDR/port pair.
# Include all resolved endpoints and advertised Kafka brokers, with external
# routes/provider firewalls configured independently. This does not create data
# services, credentials, inbound listeners, or TLS configuration.
# variables.tf rejects unrestricted IPv4 destinations and invalid ports; update
# the inputs when provider endpoint ranges change.
# for_each uses input-list indexes as keys: reordering the list changes which rule
# an index describes and may replace rules even if the allowed endpoint set is unchanged.
resource "aws_vpc_security_group_egress_rule" "data" {
  for_each          = { for index, rule in var.data_egress_rules : tostring(index) => rule }
  security_group_id = aws_security_group.host.id
  ip_protocol       = "tcp"
  from_port         = each.value.port
  to_port           = each.value.port
  cidr_ipv4         = each.value.cidr
}
