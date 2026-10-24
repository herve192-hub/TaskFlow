#!/bin/bash
set -euo pipefail
umask 077

dnf upgrade --refresh -y
dnf install -y docker python3 awscli-2
systemctl enable --now docker amazon-ssm-agent

# Compose must support !override/!reset (2.24.4+). Verify the release checksum.
compose_version=v2.39.4
install -d -m 0755 /usr/local/lib/docker/cli-plugins
compose_tmp=$(mktemp -d)
trap 'rm -rf "$compose_tmp"' EXIT
curl --fail --location --silent --show-error --retry 5 \
  "https://github.com/docker/compose/releases/download/$compose_version/docker-compose-linux-x86_64" \
  --output "$compose_tmp/docker-compose-linux-x86_64"
curl --fail --location --silent --show-error --retry 5 \
  "https://github.com/docker/compose/releases/download/$compose_version/checksums.txt" \
  --output "$compose_tmp/checksums.txt"
(cd "$compose_tmp" && sha256sum --check --ignore-missing checksums.txt)
install -m 0755 "$compose_tmp/docker-compose-linux-x86_64" /usr/local/lib/docker/cli-plugins/docker-compose
docker compose version
install -d -m 0700 /opt/taskflow/releases /opt/taskflow/secrets

# Containers restart with Docker after reboot. A release is installed separately,
# after cloud-init completes, so user data contains no source, tokens, or secrets.
touch /opt/taskflow/host-ready
