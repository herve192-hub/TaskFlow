# Auth service

[Business services](../../README.md) · [Shared security](../../../commons/common-security/README.md)

Owns credential accounts in `taskflow-auth`, password verification, account
registration, and purpose-specific token issuance. The service listens on 8081.

## Sign-in and token issuance

```mermaid
sequenceDiagram
    participant Browser as Browser and Axios client
    participant Edge as NGINX and gateway
    participant Auth as Auth controller/service
    participant Accounts as Credential account repository
    Browser->>Edge: GET /auth/csrf
    Edge->>Auth: Initialize nonce
    Auth-->>Edge: CSRF nonce cookie
    Edge-->>Browser: Nonce cookie
    Browser->>Edge: POST /auth/login with CSRF header
    Edge->>Auth: Credentials and cookie/header pair
    Auth->>Accounts: Load canonical email account
    Accounts-->>Auth: Account and encoded password verifier
    Auth->>Auth: Verify password and account state
    Auth->>Auth: Issue access and refresh JWTs
    Auth->>Accounts: Store refresh digest and migrate legacy hash if needed
    Auth-->>Edge: Account response and HttpOnly access cookie
    Edge-->>Browser: JSON response and cookie
```

New password hashes use Argon2id. Existing bcrypt verifiers upgrade after a
successful login. Access JWTs target `taskflow-api`; refresh JWTs have a separate
audience and purpose. The stored refresh field contains a SHA-256 digest.
The browser persists account metadata, with profile initialization performed
separately through user-service.

## Production onboarding

```mermaid
flowchart TD
    startup["prod startup"] --> exists{"Administrator already exists?"}
    exists -->|"Yes"| ready["Skip bootstrap"]
    exists -->|"No"| secrets["Read protected bootstrap identity and password"]
    secrets --> validate{"Valid input and unused identity?"}
    validate -->|"No"| stop["Fail startup; never promote an existing account"]
    validate -->|"Yes"| create["Create new platform administrator with Argon2id hash"]
    create --> approved["Administrator provisions normal accounts"]
```

Production disables public registration; `POST /auth/register` requires platform
administrator authority. See the [bootstrap deployment instructions](../../../../deploy/production/README.md).
`POST /auth/logout` clears this browser's cookie; already issued bearer tokens
expire independently. Refresh-token exchange/rotation endpoints, account recovery,
MFA/SSO, and global token revocation are not implemented.
