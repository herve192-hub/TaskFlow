# 🚀 TaskFlow

<p align="center">
  <img src="frontend/src/assets/TaskFlow%20Gradient%20Flow%20Logo.png" alt="TaskFlow logo" width="180" />
</p>

A cloud-native task management platform built with **React.js**, **Spring Boot Microservices**, and **MongoDB**.

TaskFlow enables users to create projects, manage tasks, track progress through a Kanban workflow, and collaborate efficiently. The application is designed using modern microservice principles, including API Gateway routing, JWT authentication, event-driven communication, containerization, and independent service deployment.

---

## ✨ Features

* User Registration and Login
* JWT Authentication & Authorization
* Project Management
* Task Creation, Assignment, and Tracking
* Kanban Board Workflow (To Do, In Progress, Done)
* Task Priorities and Due Dates
* Dashboard with Task Metrics
* RESTful APIs
* Event-Driven Notifications
* Responsive User Interface
* Dockerized Services

---

## 🏗 Architecture

```text
Frontend (React)
        │
        ▼
API Gateway
        │
 ┌──────┼──────────────┐
 │      │              │
 ▼      ▼              ▼
Auth   User         Task
Svc    Svc          Svc
 │
 ▼
MongoDB

       ▼
Notification Service
       │
 RabbitMQ / Kafka
```

### Microservices

| Service              | Responsibility                           |
| -------------------- | ---------------------------------------- |
| API Gateway          | Request Routing, Security, Rate Limiting |
| Auth Service         | Registration, Login, JWT Management      |
| User Service         | User Profiles and Preferences            |
| Task Service         | Task CRUD Operations                     |
| Project Service      | Project Management                       |
| Notification Service | Email and Event Notifications            |

---

## 🛠 Tech Stack

### Frontend

* React.js
* TypeScript
* Redux Toolkit
* React Router
* Material UI
* Axios

### Backend

* Java 21
* Spring Boot 4.1.1
* Spring Security
* Spring Cloud Gateway (Spring Cloud 2025.1.3)
* Spring Data MongoDB
* JWT Authentication

### Database

* MongoDB

### Messaging

* RabbitMQ / Kafka

### DevOps

* Docker
* Docker Compose
* Kubernetes (Planned)
* GitHub Actions

---

## 📂 Project Structure

```text
taskflow/
│
├── frontend/
│   ├── src/
│   ├── public/
│   └── package.json
│
├── backend/
│   ├── gateway-service/
│   ├── auth-service/
│   ├── user-service/
│   ├── task-service/
│   ├── project-service/
│   └── notification-service/
│
├── docker-compose.yml
└── README.md
```

---

## 📸 Screenshots

### Login Page

> Add screenshot here

### Dashboard

> Add screenshot here

### Kanban Board

> Add screenshot here

---

## 🎯 Learning Objectives

This project demonstrates:

* Full-Stack Development
* Microservices Architecture
* REST API Design
* JWT Authentication and Authorization
* MongoDB Data Modeling
* Event-Driven Communication
* Docker Containerization
* Modern React Development
* Scalable System Design

---

## 🚀 Getting Started

### Prerequisites

* Eclipse Temurin 21.0.12.1+1
* Node.js 24.21.0 (pinned in `frontend/.nvmrc`)
* MongoDB 8.0.5 (pinned in `docker-compose.yml`)
* Docker

### Clone Repository

```bash
git clone https://github.com/yourusername/taskflow.git
cd taskflow
```

### Frontend

With nvm installed, the checked-in version file selects Node.js 24.21.0:

```bash
cd frontend
nvm install
nvm use
npm ci
npm run dev
```

### Backend Services

```bash
cd backend/auth-service
mvn spring-boot:run
```

Repeat for other services.

### Docker

```bash
docker compose up --build
```

The frontend runs as `taskflow-web` at http://localhost:5173. Set `FRONTEND_PORT`
in the root `.env` file to change its port. Its API URL defaults to
`http://localhost:${API_GATEWAY_PORT}`; set `VITE_API_BASE_URL` in the root `.env`
file to override it. Rebuild `taskflow-web` after changing the API URL because
Vite embeds it in the frontend build.

---

## Continuous integration

GitHub Actions runs backend and frontend tests and security scans. See
[the CI guide](.github/CI.md) for the checks, failure thresholds, and local commands.

### Files safe to publish

This repository's source code is public. Commit application source, tests, reviewed
configuration templates, lockfiles, build definitions, CI scripts, documentation,
and the TaskFlow logo. Keep real user/customer data and production configuration
outside the source tree. No `.env` files, including `.env.example` templates,
belong in Git. The reviewed production template is `environment.example`; copy
it to an ignored `.env` file for local deployment configuration.

The ignore rules exclude environment files, credentials, keys, local databases,
backups, dependencies, compiled output, logs, browser recordings, scanner reports,
and local editor/agent state. Docker build contexts exclude these private files
as well. Ignoring a file does not remove an already tracked file or Git history.
Frontend `VITE_*` variables are embedded in the public browser bundle; use them
only for public settings, never for passwords, keys, or private endpoints.

Install **Gitleaks 8.30.1** with the setup command below. It downloads the
[official release](https://github.com/gitleaks/gitleaks/releases/tag/v8.30.1),
verifies its published checksum and version, and installs it under Git's persistent
local metadata directory. The binary is never committed or pushed. The setup also
replaces stale scanner paths left in local Git configuration. Hooks perform no
downloads; rerun setup if the scanner is missing. Then enable the versioned hooks
and review all pending publishable files:

```bash
python3 .github/scripts/install-gitleaks.py
git config --local core.hooksPath .githooks
python3 .github/scripts/verify-publishable-files.py --include-untracked --secrets --history
git diff --cached --stat
```

For a manual installation, put the checksum-verified version on `PATH` or set
`git config --local taskflow.gitleaksPath /absolute/path/to/gitleaks` to a persistent
location. `GITLEAKS_BIN` explicitly overrides scanner selection, including in CI.

The pre-commit hook scans the actual index. The pre-push hook checks each outgoing
tree and all locally available history. Both require the pinned scanner and fail
when a forbidden file or detected secret is found; CI repeats these checks.
Hooks are local and can be bypassed, and secret scanners cannot identify every kind
of sensitive data. Review staged changes before committing and enable GitHub secret
scanning/push protection and required security checks in repository settings.
Rotate any exposed credential immediately; removing it from the next commit does
not remove it from old commits, forks, or clones.

---

## 🔐 Authentication

TaskFlow uses:

* JWT Access Tokens
* Refresh Tokens
* Spring Security
* BCrypt Password Hashing
* Role-Based Access Control (RBAC)

### Roles

* USER
* ADMIN

### Project teams

Each project has its own team. Creating a project automatically makes you its
owner. Open **Teams** in the dashboard, or select **View team** on a project,
to view members. Owners and project admins can add registered users by searching
for their name or email, assign an Admin, Manager, Member, or Guest role, and
remove members. The owner cannot be removed or have their role changed through
team management. Adding a member gives them access to the project and its tasks;
removing them revokes that access.
Registration and sign-in create a user profile when one is missing. Existing
accounts without a profile should sign in again before being added to a team.

---

## 🔮 Future Enhancements

* Real-Time Updates with WebSockets
* Team Collaboration Features
* File Attachments
* Activity Logs
* OAuth2 (Google/GitHub Login)
* Automated deployments
* Kubernetes Deployment
* Monitoring with Prometheus & Grafana

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome.

Feel free to fork the repository and submit a pull request.

---

## 📄 License

This project is licensed under the MIT License.

---

## 👨‍💻 Author

**Herve Chendjou**

Software Engineer focused on:

* Java & Spring Boot
* React.js
* Cloud Technologies
* Microservices Architecture
* Full-Stack Development
