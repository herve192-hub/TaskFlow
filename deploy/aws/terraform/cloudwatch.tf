# Application log storage and infrastructure health alerts.
# Docker writes logs directly through ../compose.yml's awslogs driver; this file
# does not install a log agent. iam.tf grants writes, and outputs.tf supplies the
# group name to ../taskflow.py. Alarms report failures without repairing the host.

# Central log destination for stdout/stderr from all eight application containers.
# The Docker daemon creates streams and appends events using the EC2 role in
# iam.tf. Operators need separate permissions to read logs.
# Thirty-day retention expires older events; destroying the group removes retained
# logs. Review retention and deletion against your audit requirements.
# The group does not use kms.tf's release key. Nonblocking buffers in ../compose.yml
# protect application availability but can drop logs if delivery stays unavailable.
resource "aws_cloudwatch_log_group" "application" {
  name              = "/taskflow/${var.name}"
  retention_in_days = 30
}

# Report EC2 system/instance status-check failures for the one application host.
# InstanceId follows aws_instance.host in ec2.tf when the host is replaced.
# Maximum > 0 for two consecutive one-minute periods enters ALARM; missing metrics
# also count as failure, which can produce alarms during startup or shutdown.
# Existing alarm_topic_arns receive ALARM/recovery notifications. Empty lists still
# create the alarm but notify nobody; this module creates no SNS subscriptions.
# This checks EC2 status, not application health, memory, or disk capacity, and
# does not restart containers, recover the instance, or provision another host.
resource "aws_cloudwatch_metric_alarm" "host_status" {
  alarm_name          = "${var.name}-host-status"
  namespace           = "AWS/EC2"
  metric_name         = "StatusCheckFailed"
  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 2
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  dimensions          = { InstanceId = aws_instance.host.id }
  alarm_actions       = var.alarm_topic_arns
  ok_actions          = var.alarm_topic_arns
  treat_missing_data  = "breaching"
}

# Report unhealthy registered targets for the ALB/target-group pair from lb.tf.
# The dimensions require ARN suffixes, not full ARNs or resource display names.
# Maximum > 0 for two one-minute periods, or missing data, enters ALARM.
# lb.tf probes /healthz; ../nginx.conf forwards that request to gateway readiness.
# This path does not prove every business operation or external database works.
# Use existing alarm_topic_arns for notifications; no automatic failover/remediation
# is attached. The application still has a single EC2 target.
resource "aws_cloudwatch_metric_alarm" "unhealthy_targets" {
  alarm_name          = "${var.name}-unhealthy-targets"
  namespace           = "AWS/ApplicationELB"
  metric_name         = "UnHealthyHostCount"
  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 2
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  dimensions          = { LoadBalancer = aws_lb.application.arn_suffix, TargetGroup = aws_lb_target_group.application.arn_suffix }
  alarm_actions       = var.alarm_topic_arns
  ok_actions          = var.alarm_topic_arns
  treat_missing_data  = "breaching"
}
