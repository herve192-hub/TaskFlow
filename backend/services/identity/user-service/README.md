# User service

[Business services](../../README.md) · [Frontend profile initialization](../../../../frontend/README.md)

Owns profile and directory data in `taskflow-users`, separately from credential
accounts. The service listens on 8082.

## Identity, storage, and response views

```mermaid
flowchart TB
    browser["Browser with account cookie"] --> filter["JWT filter: canonical account subject"]
    backend["Backend caller with forwarded Bearer token"] --> filter
    filter --> self["/api/v1/users/me: own profile"]
    filter --> private["/api/v1/users/:id: self/admin check"]
    filter --> directory["Search and batch directory lookup"]
    filter --> internal["/internal/users: account-ID lookups"]
    self --> service["UserService"]
    private --> service
    directory --> service
    internal --> service
    service --> profiles[("Profile documents: id and authUserId")]
    profiles --> full["Full profile view"]
    profiles --> summary["Directory summary"]
    profiles --> internalView["Internal response"]
```

`id` is the local profile document ID. `authUserId` matches the auth account/JWT
subject and is used by membership and assignment references. Profile creation
binds the submitted email to the signed account email. Full profile reads by ID
require ownership or platform administrator authority; directory summaries support
collaboration within the deployment's single organization.

Directory search bounds input length and treats it as literal text. Batch lookup
accepts up to 100 canonical account IDs. Internal routes are authenticated and
absent from the gateway allowlist. Deactivating a profile changes its status only;
it does not disable the credential account or revoke issued JWTs.
