# Business services

[Backend](../README.md) · [Infrastructure](../infrastructure/README.md)

## Service interactions and data ownership

```mermaid
flowchart LR
    gateway["API gateway"] --> auth["Auth service"]
    gateway --> users["User service"]
    gateway --> projects["Project service"]
    gateway --> tasks["Task service"]
    auth --> accounts[("Credential accounts")]
    users --> profiles[("Profiles")]
    projects --> projectData[("Projects and memberships")]
    tasks --> taskData[("Tasks and comments")]
    projects -->|"Check profile existence"| users
    tasks -->|"Caller access and assignee membership"| projects
    tasks -->|"Assignee profile existence"| users
    tasks -->|"Lifecycle messages"| kafka["Kafka"]
    accounts -.->|"Canonical account ID"| profiles
    accounts -.->|"Owner/member references"| projectData
    accounts -.->|"Creator/assignee references"| taskData
```

Dashed account links are logical ID relationships across service-owned databases,
not automatic joins or foreign-key enforcement. The browser initializes a missing
profile after registration/sign-in; auth-service does not directly create it.

| Subsystem | Documentation |
| --- | --- |
| Credentials, password storage, and token issuance | [Auth](identity/auth-service/README.md) |
| Profiles, directory views, and private-profile access | [Users](identity/user-service/README.md) |
| Ownership, membership, and current access decisions | [Projects](work-management/project-service/README.md) |
| Assignments, state transitions, comments, and Kafka | [Tasks](work-management/task-service/README.md) |

Platform authorities and project roles are separate. Removing a member revokes
project/task access through current membership checks. Globally revoking issued
bearer tokens is separate remaining work in the [security baseline](../../SECURITY.md).
