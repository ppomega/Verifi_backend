# BGV Platform API

> The secure workflow engine behind background-verification cases — candidates, documents, checks, and AI-assisted case support.

![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)

## What it does

`bgv-platform` provides the REST API for a modern background-verification workspace. It keeps every candidate case organized from registration through clearance, with a complete document and verification trail.

| Capability | Highlights |
| --- | --- |
| Candidate cases | Create, update, track, and close candidate records |
| Document vault | Multipart upload, download, validation, and local-file storage |
| Verification checks | Identity, address, education, employment, credit, reference, and criminal-record workflows |
| AI assistance | Gemini-powered document extraction and candidate-case support chat |

## Stack

- **Java 17** and **Spring Boot 3.4**
- **Spring Web, Validation, and Spring Data JPA**
- **PostgreSQL** for case metadata
- **Spring AI + Google Gemini** for extraction and support features
- Local filesystem storage for uploaded files

## Start in minutes

### 1. Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 14+ running locally
- A Gemini API key for the AI features (optional for non-AI workflows)

### 2. Create the database

```sql
CREATE DATABASE bgvdb;
```

### 3. Configure local values

Create or update `src/main/resources/application-local.yml` — it is deliberately ignored by Git:

```yaml
spring:
  datasource:
    username: postgres
    password: your_local_password
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
```

You can instead export `GEMINI_API_KEY` in your shell. Never commit credentials.

### 4. Run

```bash
mvn spring-boot:run
```

The API starts at `http://localhost:8080`. The Angular application is configured to connect from `http://localhost:4200` (Vite on `5173` is also allowed for local development).

## API at a glance

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/candidates` | Open a candidate case |
| `GET` | `/api/candidates` | List all cases |
| `GET` | `/api/candidates/{id}` | Get a case |
| `PATCH` | `/api/candidates/{id}/status` | Change the overall case status |
| `POST` | `/api/candidates/{id}/documents` | Upload a document (`multipart/form-data`) |
| `GET` | `/api/candidates/{id}/documents` | List case documents |
| `GET` | `/api/documents/{id}/download` | Download a stored document |
| `POST` | `/api/documents/{id}/extraction` | Extract document data with AI |
| `POST` | `/api/candidates/{id}/verifications` | Add a verification check |
| `PUT` | `/api/verifications/{id}` | Update a verification check |
| `POST` | `/api/candidates/{id}/support/chat` | Ask the AI about a candidate case |

An importable request collection is available in `../postman/BGV-Platform.postman_collection.json`.

## Document storage

Uploaded bytes are stored outside the database at:

```text
uploads/{candidateId}/{uuid}.{extension}
```

Only metadata and the relative path live in PostgreSQL. By default, the API accepts `pdf`, `jpg`, `jpeg`, `png`, `doc`, and `docx` files, up to **20 MB**. These settings are configurable in `application.yml`.

## Project map

```text
src/main/java/com/bgv/platform/
├── controller/   # REST endpoints
├── service/      # case, file-storage, AI, and workflow logic
├── model/        # JPA entities and enums
├── dto/          # request and response shapes
└── config/       # CORS and application configuration
```

## Useful commands

```bash
# Run tests
mvn test

# Build the executable JAR
mvn clean package

# Run the packaged application
java -jar target/bgv-platform-1.0.0.jar
```

## Production checklist

Before handling live candidate data, add authentication and role-based authorization, database migrations (Flyway or Liquibase), antivirus scanning for uploads, audit logging, secret management, and durable object/volume storage. `ddl-auto: update` is intended for local development only.

---

Built for fast, traceable, people-first verification workflows.
