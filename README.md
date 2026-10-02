# 🚀 TaskFlow

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
* Spring Boot
* Spring Security
* Spring Cloud Gateway
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
* GitHub Actions (Planned)

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

* Java 21+
* Node.js 22+
* MongoDB
* Docker

### Clone Repository

```bash
git clone https://github.com/yourusername/taskflow.git
cd taskflow
```

### Frontend

```bash
cd frontend
npm install
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
* CI/CD Pipeline
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
