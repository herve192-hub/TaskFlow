# API gateway

[Infrastructure](../README.md) · [Route configuration](../../config-repo/api-gateway.yml)

## Public routing boundary

```mermaid
flowchart LR
    ingress["NGINX"] --> gateway["Spring Cloud Gateway: 8080"]
    config["Config Server route allowlist"] -.-> gateway
    eureka["Eureka service addresses"] -.-> gateway
    gateway -->|"/auth/**"| auth["lb://auth-service"]
    gateway -->|"/api/v1/users/**"| users["lb://user-service"]
    gateway -->|"/api/v1/projects/**"| projects["lb://project-service"]
    gateway -->|"/api/v1/tasks/**"| tasks["lb://task-service"]
    gateway -->|"No matching public route"| missing["Unlisted paths are not routed"]
```

Automatic discovery-based route publication is disabled. Eureka resolves addresses
for the configured `lb://` destinations; it does not expose every registered
application. `/internal/**`, Config Server, and Eureka are absent from the public
route list. Each business service verifies tokens and resource permissions.

`TASKFLOW_WEB_ORIGIN` supplies the credentialed browser origin. CORS permits
`X-XSRF-TOKEN`, and response-header deduplication handles headers also set by
downstream services. Production TLS and request rate limits are implemented at
[NGINX](../../../deploy/production/README.md).
