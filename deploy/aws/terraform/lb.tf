# Browser ingress: HTTP redirect -> HTTPS with ACM -> NGINX over HTTP on EC2.
# security.tf supplies ALB-to-host rules, ec2.tf supplies the sole application target,
# and route53.tf optionally publishes the hostname. TLS ends at the ALB; the VPC
# hop to host port 8080 is plaintext. Certificates and extra hosts are not created.

# Internet-facing Application Load Balancer across the supplied public subnets.
# The subnets must have working Internet Gateway routes and sufficient addresses;
# data.tf/ec2.tf check VPC membership and AZ spread, not those routing conditions.
# The ALB security group in security.tf admits public 80/443 only.
# outputs.tf exports its DNS name; cloudwatch.tf uses its ARN suffix for metrics.
# Deletion protection requires deliberate disabling before destruction/replacement.
# Invalid-header dropping and strict desync handling reduce malformed HTTP traffic.
# Preserve Host for application routing and append the observed client to XFF:
# ../nginx.conf trusts the ALB subnet CIDRs and consumes that last address for
# client rate limits. Altering append mode changes this trust contract.
# The public exposure below is intentional and is the scoped Trivy exception.
// Intentionally public browser ingress. EC2 and all backend ports stay restricted.
//trivy:ignore:AWS-0053
resource "aws_lb" "application" {
  name                       = var.name
  internal                   = false
  load_balancer_type         = "application"
  security_groups            = [aws_security_group.alb.id]
  subnets                    = var.public_subnet_ids
  enable_deletion_protection = var.protect_resources
  drop_invalid_header_fields = true
  desync_mitigation_mode     = "strictest"
  preserve_host_header       = true
  xff_header_processing_mode = "append"
}

# Define the HTTP routing and health contract for NGINX on host port 8080.
# The attachment below registers EC2, not individual internal microservices;
# ../compose.yml publishes the host port to NGINX's container port.
# ../nginx.conf proxies /healthz to gateway readiness. Compose checks other
# containers separately; this probe does not exercise all business operations.
# Probe every 30 seconds, allow five seconds, and accept only HTTP 200. An unhealthy
# target needs two successes to recover; three failures mark a target unhealthy.
# AWS uses its own initial-registration health behavior.
# The 45-second delay drains deregistered targets; container restarts do not
# deregister EC2, so this alone does not make Compose releases interruption-free.
# With only unhealthy targets, ALB can fail open; health is not access control.
# Reference: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/target-group-health-checks.html
resource "aws_lb_target_group" "application" {
  name                 = var.name
  port                 = 8080
  protocol             = "HTTP"
  target_type          = "instance"
  vpc_id               = var.vpc_id
  deregistration_delay = 45
  health_check {
    path                = "/healthz"
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

# Register the application's EC2 instance ID in the target group on port 8080.
# Instance targeting routes to the instance's private network address.
# security.tf must permit both ALB egress and host ingress on this port.
# A replacement instance updates the attachment, but successful Terraform creation
# does not mean user-data.sh or the later application release is healthy yet.
resource "aws_lb_target_group_attachment" "host" {
  target_group_arn = aws_lb_target_group.application.arn
  target_id        = aws_instance.host.id
  port             = 8080
}

# Accept browser TLS on 443 with the supplied, already-issued ACM certificate.
# The certificate must cover domain_name and belong to this ALB's region; issuance,
# validation records, and renewal prerequisites are managed outside this module.
# The named security policy permits TLS 1.2/1.3. Accepted traffic uses the default
# forward action to the NGINX target group; no host/path listener rules are added.
# NGINX and the gateway provide application routing. Target HTTP is not encrypted.
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.application.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.certificate_arn
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.application.arn
  }
}

# Accept plain HTTP on port 80 and permanently redirect the browser to HTTPS 443.
# This listener never forwards HTTP application traffic to the host target group.
# Unspecified host/path/query components retain the incoming request values.
# It pairs with security.tf's port-80 ingress rule; changing only one side either
# breaks redirects or leaves an open port with no listening service.
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.application.arn
  port              = 80
  protocol          = "HTTP"
  default_action {
    type = "redirect"
    redirect {
      protocol    = "HTTPS"
      port        = "443"
      status_code = "HTTP_301"
    }
  }
}
