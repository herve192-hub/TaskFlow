# TaskFlow source guide

Authored code and configuration contain inline explanations of purpose, ownership,
inputs, lifecycle, and relationships. Start with the file/class description, then
read the comments beside permission checks, request orchestration, state changes,
and deployment boundaries. Existing subsystem READMEs provide the wider diagrams
and operating instructions.

## Where to begin

| Area | Entry points and responsibilities |
| --- | --- |
| Browser startup and routes | [`main.tsx`](../frontend/src/main.tsx) installs global providers; [`AppRoutes.tsx`](../frontend/src/routes/AppRoutes.tsx) connects public pages and the protected workspace. See the [frontend guide](../frontend/README.md). |
| Browser requests and identity | [`axios.ts`](../frontend/src/api/axios.ts) owns cookie/CSRF transport; [`session.ts`](../frontend/src/features/auth/session.ts) caches display metadata; feature services coordinate API requests and safe session updates. |
| Shared backend policy | [`common-security`](../backend/commons/common-security/README.md) validates access tokens, cookie/CSRF requests, and infrastructure authentication. Services import this library explicitly. |
| Public service routing | The [infrastructure guide](../backend/infrastructure/README.md) explains NGINX, gateway, Config Server, and Eureka. [`config-repo/api-gateway.yml`](../backend/config-repo/api-gateway.yml) owns the gateway route allowlist. |
| Business operations | The [services guide](../backend/services/README.md) links auth, users, projects, and tasks. Controllers own HTTP contracts, service implementations own policy/orchestration, repositories own local database queries, and DTOs define exposed data. |
| Build and verification | [`backend/pom.xml`](../backend/pom.xml) manages the Java reactor; the [frontend package/command guide](../frontend/README.md#package-and-compiler-files) explains npm/compiler inputs. [CI documentation](../.github/README.md) connects workflows, helper scripts, and reports. |
| Local containers | [`docker-compose.yml`](../docker-compose.yml) builds local services plus MongoDB/Kafka with persistent development volumes. It loads the ignored local environment. |
| Production containers | The [production guide](../deploy/production/README.md) explains the independent hardened Compose stack, external TLS data services, scoped secrets, and one-time administrator provisioning. |
| AWS deployment | The [AWS guide](../deploy/aws/README.md) connects the documented Terraform resources, ECR/S3 release bundle, ALB ingress, EC2 host, Secrets Manager, and [`taskflow.py`](../deploy/aws/taskflow.py). |
| Publication protection | [Hooks](../.githooks/pre-commit) invoke [`verify-publishable-files.py`](../.github/scripts/verify-publishable-files.py) against selected Git content; ignore files exclude private/generated material from normal staging. |

## Contracts that cross files

* **Identity:** the auth account ID is the JWT subject and the reference used by
  membership/assignment records. Profile IDs and membership document IDs identify
  different records. Public directory summaries omit private profile contact data.
* **Authorization:** platform authorities and project roles are separate. Browser
  controls improve navigation; services validate every protected operation. Task
  operations consult current project access through internal HTTP calls.
* **Browser credentials:** authentication uses an HttpOnly cookie; unsafe requests
  echo the readable CSRF nonce in a header. Session storage contains display
  metadata only. Build-time `VITE_*` values are public.
* **Configuration:** local `application.yaml` bootstraps each service; native
  Config Server supplies shared/service settings; deployment environment and scoped
  file imports supply protected values. Keep port, route, property-name, and secret
  target changes coordinated with Compose and ingress configuration.
* **Persistence/events:** each business service owns its logical MongoDB database.
  Cross-service IDs are references rather than database foreign keys. Current task
  status/completion publication is asynchronous and has no durable outbox; database
  persistence and Kafka delivery are not one atomic transaction.
* **Pagination/time:** endpoint DTOs document their page-field conventions. Workspace
  aggregation loads all pages before deriving totals. API timestamps represent
  instants; saved account preferences control their display.

## Files documented without modifying their contents

| Files | Reason and maintenance rule |
| --- | --- |
| `frontend/package.json`, `package-lock.json` | JSON manifests cannot carry normal comments. The [frontend guide](../frontend/README.md#package-and-compiler-files) explains dependencies, commands, lock integrity, and consumers. Regenerate the lock through npm. |
| `frontend/.nvmrc` | A plain Node version consumed by version managers and CI; its relationship to the Docker builder is documented in the frontend guide. |
| Maven `mvnw` / `mvnw.cmd` launchers | Upstream wrapper code, including its own vendor comments. Use the [backend build guide](../backend/README.md#build-and-test); update launchers through the wrapper tooling, not application documentation edits. Root `.mvn/wrapper/maven-wrapper.properties` selects the Maven distribution. |
| Terraform lock file | Provider versions/checksums generated by Terraform; its existing header describes this contract. Resource explanations live in the grouped `.tf` files. |
| `deploy/aws/terraform/user-data.sh` | Host bootstrap script intentionally kept free of additional documentation comments, as requested. Its existing contents are preserved; [AWS setup instructions](../deploy/aws/README.md) and inline Terraform template consumers explain how it is used. |
| PNG/SVG branding and screenshots | Visual assets have no application logic. `TaskFlowLogo.tsx` documents cropping/reuse of the original artwork; the root README labels screenshots. Update artwork and viewport bounds together. |
| License, generated output, dependency caches, local secrets | Preserve legal text. Generated output and vendor dependencies are not maintained application source. Private runtime inputs are excluded from publication and are described using names/placeholders only. |

## Maintaining inline explanations

Explain the reason and contract, rather than narrating syntax. For a new class,
component, script, or configuration block, cover what it owns, who calls/loads it,
where inputs originate, which IDs or secrets it handles, what failures mean, and
which related files must change with it. Add local comments for non-obvious policy,
ordering, cleanup, concurrency, and transaction boundaries. Tests should describe
their invariant and fixture isolation.

Keep explanations faithful to implemented behavior. Reserved DTOs, empty utility
classes, admin scaffolds, and declared-but-unpublished event types are identified
as such in their source; they do not imply working APIs. Update comments when
behavior changes, and keep real credentials, copied provider output, environment
values, and private data out of examples.
