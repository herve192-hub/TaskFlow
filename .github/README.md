# TaskFlow CI and repository protection

[System overview](../README.md) · [Detailed checks and local commands](CI.md) ·
[Full-stack environment](e2e/README.md)

## Independent workflows and job dependencies

```mermaid
flowchart LR
    trigger["Matching push/PR, weekly schedule, or manual run"]
    trigger --> backendTests
    trigger --> frontendTests
    trigger --> npmAudit
    trigger --> codeql
    trigger --> trivy
    trigger --> history
    trigger --> e2e
    subgraph backend["Backend CI"]
        backendTests["Surefire + Failsafe with MongoDB; package reactor"]
        backendTests --> backendImages["Build/scan seven service images"]
    end
    subgraph frontend["Frontend CI"]
        frontendTests["Lint, Vitest suites, type check, build, browser smoke"]
        frontendTests --> frontendImage["Build/scan NGINX runtime image"]
        npmAudit["Independent npm dependency audit"]
    end
    subgraph security["Security"]
        codeql["CodeQL: Java, JS/TS, and Actions"]
        trivy["Trivy: repository and packaged Java dependencies"]
        history["Publication policy and complete fetched Git history scan"]
    end
    subgraph fullstack["Full-stack E2E"]
        e2e["Build and start isolated real services"]
        e2e --> scenario["Browser collaboration, authorization, persistence, Kafka"]
        scenario --> tls["HTTPS fixture and production Compose validation"]
        tls --> cleanup["Logs, teardown, and report artifacts"]
    end
```

The four workflows run independently; E2E does not depend on a successful run of
the separate backend/frontend workflows. Within each component workflow, image
scanning depends on that workflow's test/build job. E2E cleanup/log collection
is attempted with `always()` even when earlier steps fail.

Backend, frontend, and E2E pushes/PRs use path filters; the Security workflow
has none. Schedules run Mondays at 08:23 UTC. Test/build/scanner failures fail
their jobs; high/critical dependency/source findings and any detected secret
enforce the configured security gates. Trivy reports are redacted before upload.
Required merge checks are configured separately in GitHub repository rules.

## Protection before a push

```mermaid
flowchart LR
    edit["Working tree"] --> stage["Git index"]
    stage --> commit["pre-commit: file policy and indexed-content scan"]
    commit --> history["Local commits"]
    history --> push["pre-push: outgoing trees and available history"]
    push --> github["GitHub source and CI checks"]
    forbidden["Environment files, keys, data, generated output"] -.-> blocked["Publication policy blocks tracked forbidden paths"]
    commit -.-> blocked
    push -.-> blocked
```

Install the pinned scanner with `python3 .github/scripts/install-gitleaks.py` from
the repository root, then enable `.githooks` as documented in the
[publishing policy](../README.md#files-safe-to-publish). The scanner lives in Git's
local metadata, outside pushed source. Hooks are local controls and can be bypassed;
CI repeats their file/secret policy after source reaches GitHub.
