# TaskFlow CI

Backend CI and Frontend CI run for pushes to `main`, `develop`, and
`dev/feature-workflows`, and pull requests targeting `main` or `develop`. Their
path filters include their source directory, all workflow files, CI scripts, and
the root Compose file. Each workflow also supports manual runs and runs every Monday at
08:23 UTC to recheck dependencies and container images.

The Security workflow runs on those pushes and pull requests without path
filters, every Monday at 08:23 UTC, and manually. The schedule analyzes the default
branch so newly published dependency advisories are checked without a code change.

## Checks

| Check | What runs |
| --- | --- |
| Backend unit tests | Maven Surefire runs unit and standalone controller tests. |
| Backend integration tests | Maven Failsafe runs all `*ApplicationTests`, `IT*`, `*IT`, and `*ITCase` classes during `verify`. CI provides an isolated MongoDB 8 container. `TaskRepositoryIT` exercises persistence, project isolation, archived-task filtering, duplicate-title checks, and status queries against MongoDB. |
| Frontend unit tests | Vitest tests API error handling and user display names. |
| Frontend integration tests | React Testing Library tests protected routes together with React Router and browser storage. |
| Browser smoke tests | Playwright Chromium checks the production build's sign-in form, registration navigation, and redirects for unauthenticated protected pages. These tests do not submit authentication requests or exercise the complete backend stack. |
| Lint and build | Frontend ESLint, TypeScript checking, Vite production build, and backend Maven packaging. |
| Coverage | JaCoCo reports for backend modules, and separate Vitest V8 reports for frontend unit and integration suites. Reports establish the current baseline; no minimum coverage percentage is imposed. |
| Static security analysis | CodeQL's `security-extended` queries analyze Java, JavaScript/TypeScript, and GitHub Actions workflows. Results are uploaded to GitHub code scanning and retained as SARIF artifacts. |
| Dependency security | npm audit includes frontend development, optional, and peer dependencies. Trivy's filesystem scan covers manifests, including npm development dependencies; a separate rootfs scan inspects packaged backend JARs and their transitive Java dependencies. The security jobs install internal Maven modules into the local cache and avoid remote dependency identification requests during the Trivy scan. Vulnerability databases still refresh normally. |
| Secrets | Gitleaks scans all fetched Git history, with redacted results. Trivy scans the current checkout for secrets. |
| Configuration security | Trivy checks every Dockerfile for security misconfigurations; CodeQL checks GitHub Actions workflows. |
| Container security | Every backend service image and the frontend runtime image are built and scanned for OS and application dependency vulnerabilities. |
| Dependency maintenance | Dependabot checks GitHub Actions, frontend npm packages, backend Maven dependencies, Docker base images, and Compose images weekly. |

Test, build, lint, and scanner execution failures fail their jobs. npm audit,
Trivy, and the CodeQL SARIF check fail on high or critical findings. Gitleaks fails
on any detected secret. No scanner findings are ignored or allowed to fail silently.
CodeQL findings below the threshold remain available in GitHub code scanning.
Trivy findings are also printed in the job log and GitHub job summary, including
affected files, package versions, and fixed versions. Repeated dependency findings
are grouped. Secret summaries show only the rule and location, never the match.
The full JSON scan report remains available in the uploaded artifact.

The backend asserts that both test suites actually executed. Frontend unit and
integration suites run separately so an empty suite fails. Reports are uploaded
even when checks fail, when the scanner or test runner produced them, and retained
for 14 days. Actions are pinned to published commit SHAs, checkout credentials are
not persisted, and only the CodeQL jobs receive `security-events: write`.

Existing vulnerabilities can make the new security jobs fail. Address each finding
with an appropriate dependency or configuration change; do not disable the gate to
make CI pass. Container scans run with Backend CI and Frontend CI, including their
weekly runs. The Security workflow covers source, repository dependencies,
configuration, and secrets.

To make these jobs block merging, configure the applicable checks in GitHub branch
rules. Account for path-filtered workflows when choosing globally required checks:
GitHub can leave a required workflow pending when its paths do not match. If CodeQL
default setup is already enabled, switch to advanced setup for this custom workflow.

## Version pins

Use exact stable releases for dependencies and tools. Container references include
both a complete release tag and a verified multi-platform manifest digest. Do not
use floating tags such as `latest`, `stable`, `lts`, or major-only runtime versions.
Compose tags locally built application images with their numeric package or Maven
artifact version and builds them locally through `pull_policy: build`, avoiding
implicit image tags. CI tags each built image with the checked-out commit SHA.

| Component | Pinned release |
| --- | --- |
| Node.js | 24.21.0, shared by local nvm setup and both frontend CI jobs through `frontend/.nvmrc`. |
| Java | Eclipse Temurin 21.0.12.1+1; CI uses Adoptium's exact SemVer identifier `21.0.12+101.0.LTS`. Backend containers use the same release on Ubuntu Noble. |
| Spring Boot / Cloud | 4.1.1 / 2025.1.3. All services inherit these shared BOM selections. |
| OpenAPI | springdoc 3.1.1 for Spring Boot 4. |
| Frontend build image | `node:24.21.0-alpine3.24`, plus its manifest digest. |
| Frontend runtime image | `nginx:1.30.5-alpine3.24`, plus its manifest digest. |
| MongoDB | 8.0.5 with the same digest in Compose and backend CI. |
| Kafka | 4.1.0, plus its manifest digest in Compose. |
| Maven | 3.9.16 in the checked-in Maven wrapper configuration. |

Frontend direct dependencies match the exact versions already resolved in
`package-lock.json`. `frontend/.npmrc` sets `save-exact=true` for future installs;
use `npm ci` for reproducible builds. Upstream dependency ranges recorded inside
the lockfile are package metadata; their actual resolved versions and integrity
hashes remain locked. Backend dependency versions are explicit or inherited from
pinned BOMs; local service artifact versions use numeric releases without
`SNAPSHOT`. Maven's `java.version` remains `21` because it selects the compiler
language level rather than downloading a JDK.

Update pinned releases through reviewed changes and keep image digests paired with
their release tags. Dependabot proposes weekly dependency and image updates. When
updating Node.js, change both `.nvmrc` and the frontend build image. When updating
Java, keep all backend Dockerfiles and the backend/security workflow selections
consistent. The release identifiers and digests were checked against the
[Docker Official Images catalog](https://github.com/docker-library/official-images/tree/master/library)
and [Temurin release metadata](https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.12.1%2B1).

After pinning, a clean `npm ci`, 70 backend unit tests, 17 frontend unit/integration
tests, 3 Chromium smoke tests, backend packaging, frontend lint/build, actionlint,
and Compose configuration validation passed. The resolved npm package graph did
not change. The subsequent migration validation below covers every application
container.

## Security remediation and validation, 2026-10-13

The repository scan originally failed on vulnerable Java dependencies and eight
Dockerfiles whose runtime stages ran as root. It wrote findings only to a JSON
artifact, so the Actions log showed scan progress followed by exit code 1 without
the findings. The workflows now always print a readable report after the scan;
the high/critical failure threshold is unchanged.

The backend now uses Spring Boot 4.1.1, Spring Cloud 2025.1.3, and springdoc 3.1.1.
Jackson 2/3, Tomcat, Bouncy Castle, FreeMarker, LZ4, and Commons FileUpload have
explicit security patch selections where the managed defaults still had findings.
The auth service uses the Spring Security 7 authentication provider constructor.
Web MVC, Kafka, and MongoDB tests use the Boot 4 starters; the MongoDB test slice
uses its relocated annotation. MongoDB connection settings use `spring.mongodb`
in application files, the Config Server repository, and CI. Task events use the
Spring Kafka Jackson 3 serializer. The environment variables inside the connection
URI placeholders remain usable.

All seven Java runtime images use UID/GID 10001 with an owned application directory
and JAR. NGINX runs as its existing unprivileged account, uses a writable PID/cache
location, and listens on container port 8080. Compose still exposes the frontend
on the same host port, 5173 by default. The pinned frontend base image needed
three additional Alpine fixes: `pcre2=10.49-r0`, `tiff=4.7.2-r0`, and
`libexpat=2.8.5-r0`.

Local validation passed all 70 backend unit tests and 10 integration tests against
an isolated MongoDB 8.0.5 container. Both repository scanning and packaged-JAR
scanning returned no high or critical findings with Trivy 0.75.0. Repository scan
coverage remained 16 language manifests and 8 Dockerfiles; it resolved 3,313
package entries. Before remediation, the same scan reported 96 distinct Java
advisory/package/version entries (77 high and 19 critical), plus 8 Dockerfile
findings. Repeated declarations across modules produced 888 dependency findings
before deduplication.

All eight runtime images were built and passed their high/critical vulnerability
scans. The non-root NGINX container served the sign-in SPA route, and the non-root
Config Server served a healthy HTTP endpoint with a read-only configuration mount.
Workflow linting and Compose configuration validation also passed.

The summary formatter was checked for duplicate grouping, severity filtering,
secret redaction, safe Markdown rendering, empty reports, and missing reports.
Scans continue to use fresh advisory databases, so future findings can fail CI.
The first GitHub Actions run of the updated workflows is still needed to verify
hosted CodeQL execution and artifact uploads.

## Initial validation, 2026-10-09

Local validation passed 70 backend unit tests, 10 backend integration tests,
14 frontend unit tests, 3 frontend integration tests, and 3 Chromium smoke tests.
Frontend lint, frontend production build, backend packaging, the workflow linter,
and the scanner failure thresholds also passed validation. npm audit reported no
vulnerabilities after updating the transitive `source-map-js` dependency. Gitleaks
found no secrets in tracked source or the 59 fetched commits.

Trivy's Java archive scan reported 81 distinct high/critical vulnerability entries
(64 high and 17 critical), deduplicated by advisory, package, and installed version.
Affected packages include Spring Framework, Spring Security, Tomcat, and Bouncy
Castle. The Dockerfile scan also reported `DS-0002` for all eight runtime images
running as root. Those pre-existing findings initially made the security gates
fail and were addressed by the subsequent migration and container changes above.
They were not suppressed.
Counts will change as dependencies and vulnerability databases update.

Those initial local database tests used MongoDB 7 after a newer MongoDB 8 image
failed to start. The migration tests above use the exact MongoDB 8.0.5 release
configured in CI on Ubuntu 24.04.

## Run locally

Backend unit tests use Eclipse Temurin 21.0.12.1+1:

```bash
cd backend
bash ./mvnw --batch-mode --no-transfer-progress clean test
```

For backend integration tests, use a dedicated local MongoDB instance, provide its
URI, and generate a temporary signing key. Config Server imports and Eureka
registration and registry fetching are disabled by the Failsafe configuration.
The Mongo credential placeholders below are test values; the URI determines the
connection's authentication settings.

```bash
cd backend
export SPRING_MONGODB_URI=mongodb://127.0.0.1:27017/taskflow-ci
export SPRING_MONGODB_HOST=127.0.0.1
export SPRING_MONGODB_DATABASE=taskflow-ci
export MONGO_INITDB_ROOT_USERNAME=ci
export MONGO_INITDB_ROOT_PASSWORD=ci-test-password
export JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"
bash ./mvnw --batch-mode --no-transfer-progress clean verify
```

Frontend checks use Node.js 24.21.0. If you use nvm, `nvm install` and `nvm use`
inside `frontend` select the checked-in version:

```bash
cd frontend
npm ci
npm run lint
npm run test:ci
npm run build
npx playwright install --with-deps --only-shell chromium
npm run test:e2e
npm audit --audit-level=high --include=dev --include=optional --include=peer
```

`npm run test:unit` and `npm run test:integration` run the individual frontend
suites. Playwright starts and stops the local Vite preview server automatically.
