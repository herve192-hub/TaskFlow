# TaskFlow security baseline

The review uses [OWASP ASVS 5.0.0](https://github.com/OWASP/ASVS/tree/v5.0.0)
as a reference for a single-organization Docker Compose deployment. The changes
implement and test a set of application and deployment controls. They do not
constitute complete ASVS verification, SOC 2/ISO certification, or production
approval. Production deployment requires the organization-specific controls below.

## Implemented controls

| Area | Control and evidence |
| --- | --- |
| Authentication | Argon2id with 19 MiB, 2 iterations, and parallelism 1; new passwords require 15–128 characters and support passphrases. Existing bcrypt hashes migrate on successful login. Auth failures and validation errors omit credential values. Unique database indexes enforce account identifiers. Production onboarding requires administrator approval, with protected initial provisioning. |
| Browser sessions | Access tokens use HttpOnly, SameSite=Strict cookies. Production requires Secure host-only `__Host-` cookies and a 1–15 minute lifetime. JavaScript stores only display/user metadata in sessionStorage. Old access/refresh tokens are removed from localStorage. Cookie-authenticated writes require a CSRF cookie/header pair. Sign-out clears the browser's access cookie. |
| Token validation | Every service validates signature, issuer, audience, access purpose, subject, token identifier, issued time, expiration, and roles. Production uses RS256 with keys of at least 3072 bits; only auth has the private key. Refresh tokens and other algorithms are rejected by API consumers. Stored refresh-token values are SHA-256 digests. |
| Authorization and privacy | Project/task permissions are enforced server-side and exercised through real services. Member removal revokes access immediately. Admin endpoints require administrator authority. Private user profiles require self/admin access; profile email must match the signed identity. User-directory summaries remain available to approved accounts within this single organization. |
| Browser protections | NGINX sets CSP with scripts limited to self, frame restrictions, nosniff, referrer and permissions policies. Inline styles remain allowed for MUI/Emotion. Production serves TLS 1.2/1.3 with HSTS. The CI ingress fixture checks certificate and hostname verification and authentication rate limiting. |
| Exposure and transport | Gateway routes are explicitly allowlisted; automatic discovery routes are disabled. Management exposure is limited to health with component/details hidden. Production publishes HTTPS only and authenticates infrastructure clients. Production startup rejects unsafe MongoDB/Kafka transport settings. |
| Containers and secrets | Application containers run without root, with read-only filesystems, dropped capabilities, no-new-privileges, bounded memory/processes, and limited temporary writable storage. Secrets are scoped file mounts, with no private signing key on consumer services. The development stack binds host ports to loopback. Image versions/digests and actions remain pinned. |
| Audit and CI | Security audit events omit credentials and request bodies. NGINX logs omit query strings. CI runs unit/integration/full-stack checks, source/dependency/image/configuration/secret scans, and weekly rescans. Scanner artifacts and container logs are redacted. Unsafe settings and security failures fail their jobs. |

Password storage follows the [OWASP password storage guidance](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).
Production configuration and secret ownership are described in
[the deployment guide](deploy/production/README.md).

## Remaining conditions before production approval

1. Integrate the organization's MFA/SSO, account recovery, identity verification,
   account lifecycle, and compromised-password policy. The current password-only
   authentication and disabled application recovery do not satisfy every
   enterprise authentication requirement. Existing short passwords need migration.
2. Implement centralized session/revocation handling if immediate global logout,
   role changes, account disablement, or incident-driven token invalidation is
   required. Already-issued bearer access tokens remain valid until expiration;
   removing a project membership is checked immediately for each request.
3. Supply trusted HTTPS certificates and verify the actual MongoDB/Kafka TLS,
   service-account privileges, encryption at rest, topic ACLs, backups, and restores.
   Add authenticated service-to-service TLS when the private Docker network cannot
   be trusted. Internal HTTP is not mTLS and is not appropriate for untrusted networks.
4. Ship security events to a protected centralized audit store with retention,
   alerts, and incident ownership. Local rotating container logs alone are not an
   immutable enterprise audit trail. Operate host/network hardening and secret,
   signing-key, and certificate rotation. Coordinate key rotation across services.
5. Verify the hosted GitHub workflows, CodeQL results, required merge checks, and
   release-image provenance. Complete threat modeling and an independent ASVS
   assessment of the organization's chosen assurance level before sign-off.

The baseline's scope is one organization per deployment; multi-tenant isolation
and federation are not implemented. The repository's CI stack is disposable test
infrastructure and must never be used with production data or credentials.
