# Mini Banking System

A production-inspired banking microservices platform built to practice real-world DevOps concepts including containerized delivery, service communication, event-driven architecture, API security, CI/CD automation, immutable releases, health validation, and operational troubleshooting.

The project focuses on DevOps and distributed-system operations rather than implementing a production core-banking system.

---

## Architecture

```text
                         Client
                           |
                           v
                      Nginx :80
                           |
                           v
                  +----------------+
                  |  API Gateway   |
                  |  JWT Security  |
                  | 127.0.0.1:8080 |
                  +----------------+
                    |      |      |
                    v      v      v
                Account Transaction Payment
                Service   Service   Service
                   |         |         |
                   v         v         v
              account-db transaction-db payment-db
              PostgreSQL  PostgreSQL   PostgreSQL

                            |
                            | transaction-created
                            v
                          Kafka
                         /     \
                        v       v
                     Audit   Notification
                     Service    Service
```

---

## Services

| Service | Responsibility |
|---|---|
| Account Service | Manages accounts and account status |
| Transaction Service | Validates accounts, creates transactions, and publishes Kafka events |
| Payment Service | Validates transactions and processes payments |
| Audit Service | Consumes transaction events for audit processing |
| Notification Service | Consumes transaction events for notification processing |
| Gateway Service | Provides centralized routing and JWT authentication |

The platform contains:

- 6 Spring Boot services
- 3 PostgreSQL databases
- 1 Kafka broker
- Nginx reverse proxy
- Docker Compose runtime

---

## Architecture Concepts

### Database per Service

Each stateful service owns its database:

```text
Account Service      -> account-db
Transaction Service  -> transaction-db
Payment Service      -> payment-db
```

Services never directly query another service's database.

This keeps service ownership clear and reduces database-level coupling.

### Synchronous Communication

Operations requiring an immediate response use HTTP communication.

```text
Transaction Service -> Account Service
Payment Service     -> Transaction Service
```

Inside Docker Compose, services communicate through Docker DNS:

```text
http://account-service:8081
http://transaction-service:8082
```

### Event-Driven Communication

After creating a transaction, Transaction Service publishes a:

```text
transaction-created
```

event to Kafka.

Audit Service and Notification Service use separate Kafka consumer groups, allowing both services to independently receive the event.

```text
Transaction Service
        |
        v
      Kafka
      /   \
     v     v
  Audit  Notification
```

---

## API Gateway and Security

The API Gateway is the central application entry point.

It provides:

- JWT authentication
- Centralized routing
- A security boundary for internal services
- A single API entry point

Internal microservices are not directly published to the host.

The Gateway binds only to:

```text
127.0.0.1:8080
```

Host Nginx exposes port `80` and reverse proxies requests to the Gateway.

An unauthenticated request to a protected endpoint returns:

```text
HTTP/1.1 401
```

---

## Containerization

The complete platform runs with Docker Compose.

Runtime components:

```text
gateway-service
account-service
transaction-service
payment-service
audit-service
notification-service

account-db
transaction-db
payment-db

kafka
```

Spring Boot applications are packaged into Docker images using Java 17 runtime images.

Application containers run as non-root users.

---

## Health Checks and Recovery

Core services expose Spring Boot Actuator health endpoints.

Health checks distinguish:

```text
Container is running
```

from:

```text
Application is healthy
```

Docker restart policies use:

```yaml
restart: unless-stopped
```

Process-level failures were tested by terminating a container's main process and verifying automatic container recovery.

---

## CI/CD Pipeline

GitHub Actions implements the delivery pipeline.

```text
Pull Request / Push
        |
        v
Detect Changed Services
        |
        v
Maven Test & Package
        |
        v
Docker Build
        |
        v
GitHub Container Registry
        |
        v
Immutable Git SHA Image
        |
        v
Self-hosted GitHub Runner
        |
        v
Selective Docker Compose Deploy
        |
        v
Post-deployment Validation
```

### Selective Builds and Deployments

The workflow detects changed service directories.

For example:

```text
payment-service/**
```

causes only:

```text
payment-service
```

to be selected.

Changes to shared deployment files such as:

```text
docker-compose.yml
.github/workflows/ci-cd.yml
.env.example
```

trigger validation of all services.

---

## Immutable Releases

Docker images are pushed to GitHub Container Registry using the Git commit SHA.

Example:

```text
ghcr.io/thaianhvu29/mini-banking-account-service:<git-sha>
ghcr.io/thaianhvu29/mini-banking-transaction-service:<git-sha>
ghcr.io/thaianhvu29/mini-banking-payment-service:<git-sha>
```

Example from the deployed environment:

```text
ghcr.io/thaianhvu29/mini-banking-transaction-service:10c7279c9fa7350eba5b42f717df81864ab03f1e
```

This makes every running application image traceable to the exact source commit that produced it.

---

## Self-Hosted Deployment Runner

CI jobs run on GitHub-hosted runners.

Deployment runs on a self-hosted GitHub Actions runner installed on the deployment server.

```text
GitHub-hosted Runner
        |
        v
Test / Build / Push GHCR
        |
        v
Self-hosted Runner
        |
        v
Docker Compose Deployment
```

The deployment workspace is separated from the development repository:

```text
~/mini-banking
    Development repository

~/actions-runner
    GitHub Actions self-hosted runner

~/mini-banking-deploy
    Deployment workspace
```

This avoids requiring GitHub-hosted runners to SSH directly into the deployment server.

---

## Post-Deployment Validation

After deployment, the workflow validates the runtime state of the selected service.

For services with Docker health checks, the pipeline waits until the service becomes:

```text
healthy
```

If a service becomes unhealthy or exits, the deployment job fails and prints recent container logs.

Services without a Docker health check must remain in the:

```text
running
```

state after startup.

---

## Environment Configuration

Create the environment configuration from the template:

```bash
cp .env.example .env
```

Important variables include:

```env
JWT_SECRET=replace-with-at-least-32-byte-secret
AUTH_USERNAME=demo-user
AUTH_PASSWORD=change-me
```

Application image references are also externalized:

```env
ACCOUNT_SERVICE_IMAGE=mini-banking/account-service:dev
TRANSACTION_SERVICE_IMAGE=mini-banking/transaction-service:dev
PAYMENT_SERVICE_IMAGE=mini-banking/payment-service:dev
AUDIT_SERVICE_IMAGE=mini-banking/audit-service:dev
NOTIFICATION_SERVICE_IMAGE=mini-banking/notification-service:dev
GATEWAY_SERVICE_IMAGE=mini-banking/gateway-service:dev
```

The real `.env` file is excluded from Git.

---

## Running the Platform

### Prerequisites

```text
Java 17
Maven
Docker
Docker Compose
Git
```

Create the environment file:

```bash
cp .env.example .env
```

Start the platform:

```bash
docker compose up -d
```

Check the runtime:

```bash
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Image}}'
```

---

## Security Validation

Protected API routes reject requests without a JWT.

Example:

```bash
curl -i http://127.0.0.1:8080/api/accounts/1
```

Expected response:

```text
HTTP/1.1 401
```

This confirms that external clients cannot bypass the Gateway authentication boundary.

---

## Troubleshooting Cases

The project intentionally included operational troubleshooting instead of focusing only on the happy path.

### Docker DNS vs Localhost

Containers cannot use host-style `localhost` addresses to reach other services.

Service-to-service endpoints were changed to Docker DNS names such as:

```text
http://account-service:8081
```

---

### Gateway Routing Configuration

An initial Spring Cloud Gateway configuration used an incompatible routing configuration and returned `404`.

The Gateway configuration was corrected for Spring Cloud Gateway Web MVC.

---

### Slow Spring Boot Startup

Containers could be:

```text
Up
```

while the Spring application was still starting.

Health checks and longer startup grace periods were used to avoid false deployment failures.

---

### Kafka Listener Configuration

Kafka clients inside Docker use:

```text
kafka:9092
```

instead of `localhost:9092`.

Correct Kafka advertised listeners were required for container-to-container connectivity.

---

### Kafka Health Check Timeout

The Kafka broker was operational, but Docker initially reported it as unhealthy because Kafka CLI-based health checks exceeded their timeout on the resource-constrained server.

The timeout, retries, and startup grace period were tuned to avoid false unhealthy states.

---

### Database and Kafka Dual Write

Transaction Service persists a transaction and then publishes a Kafka event.

This introduces a possible dual-write consistency problem if the database operation succeeds but event publishing fails.

For a production system, a Transactional Outbox pattern would be a suitable solution.

The simplified project keeps direct publishing to focus on infrastructure and delivery concepts.

---

### Docker Compose Project Identity Conflict

The deployment workspace was moved from:

```text
~/mini-banking
```

to:

```text
~/mini-banking-deploy
```

Docker Compose derives its default project name from the current directory.

As a result, the deployment initially attempted to create a new project named:

```text
mini-banking-deploy
```

while existing containers belonged to:

```text
mini-banking
```

This caused errors such as:

```text
Conflict. The container name "/account-service" is already in use
```

The issue was diagnosed using Docker Compose labels and fixed by explicitly setting:

```text
COMPOSE_PROJECT_NAME=mini-banking
```

in the deployment workflow.

---

### Deployment Server Resource Exhaustion

Running k3s, Argo CD, monitoring components, Kafka, PostgreSQL databases, multiple Java services, and the GitHub Actions runner on the same resource-constrained server caused severe resource pressure.

Observed symptoms included:

```text
load average > 60
RAM almost exhausted
swap almost completely used
CPU idle near 0%
```

The issue was diagnosed using:

```bash
uptime
free -h
vmstat
ps
docker stats
```

Unnecessary workloads were stopped to free resources before continuing deployment.

---

### Nginx Port Conflict

An attempt to run Nginx as another Docker container failed because host Nginx already owned port `80`.

The conflict was identified with host port inspection.

Instead of stopping the existing reverse proxy, the project reused host Nginx and bound the Gateway only to:

```text
127.0.0.1:8080
```

---

## Tech Stack

**Application**

- Java 17
- Spring Boot
- Spring Cloud Gateway
- Spring Security
- Spring Data JPA

**Data and Messaging**

- PostgreSQL
- Apache Kafka

**Containers and Networking**

- Docker
- Docker Compose
- Nginx

**CI/CD**

- GitHub Actions
- GitHub Container Registry
- Self-hosted GitHub Actions Runner
- Immutable Git SHA image tags
- Selective service deployment

**Operations**

- Spring Boot Actuator
- Docker health checks
- Docker restart policies
- Linux troubleshooting tools

---

## Project Goals

This project demonstrates practical experience with:

- Operating a multi-service application
- Designing service ownership boundaries
- Managing synchronous and asynchronous communication
- Securing internal services behind an API Gateway
- Building immutable container releases
- Implementing selective CI/CD
- Running self-hosted deployments
- Validating service health after deployment
- Diagnosing real container, network, resource, and CI/CD failures

---

## Disclaimer

This project is a DevOps and distributed-systems learning environment.

It is not intended to represent a production financial core-banking implementation. Production banking systems would require additional controls such as high availability, secrets management, idempotency, reconciliation, distributed transaction strategies, stronger auditing, disaster recovery, and security hardening.
