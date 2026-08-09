# TransitFlow

> **AI-powered bus route verification backend** - uses Google Gemini Vision to detect route mismatches from bus photos and alert commuters in real time.

[![Deploy to Render](https://img.shields.io/badge/Deployed%20on-Render-46E3B7?logo=render&logoColor=white)](https://transit-flow-k719.onrender.com)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M1-6DB33F?logo=spring&logoColor=white)](https://docs.spring.io/spring-ai/reference/)

---

## Overview

TransitFlow solves a common commuter problem: **a bus displaying the changed route number on its LED display**. A commuter photographs the bus, uploads it to the API, and Gemini Vision AI:

1. Reads the **desired route** from the red circular windshield sticker.
2. Reads the **actual route** from the **LED display** (what the bus is actually running).
3. Detects mismatches, validates the stop location, and creates a **spotting alert** that other commuters can query.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| AI Integration | Spring AI + Google Gemini (via OpenAI-compatible API) |
| Build Tool | Maven |
| Container | Docker (multi-stage, Eclipse Temurin 21 Alpine) |
| CI/CD | GitHub Actions → Render |

---

## Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.8+**
- A **Google Gemini API key** - get one at [aistudio.google.com](https://aistudio.google.com/app/apikey)

### Running Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/transit-flow-backend.git
   cd transit-flow
   ```

2. **Set your Gemini API key:**
   ```bash
   # Linux / macOS
   export GEMINI_API_KEY=your_api_key_here

   # Windows (PowerShell)
   $env:GEMINI_API_KEY="your_api_key_here"
   ```

3. **Build and run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The application starts on **[http://localhost:8080](http://localhost:8080)**.

### Running with Docker

```bash
docker build -t transit-flow .
docker run -p 8080:8080 -e GEMINI_API_KEY=your_api_key_here transit-flow
```

---

## Project Structure

```
transit-flow/
├── src/main/java/com/transitflow/
│   ├── controller/          # REST endpoints (Alert, Route, Spotting, RouteExtraction)
│   ├── service/             # Business logic + Gemini Vision integration
│   ├── repository/          # JSON-backed route data store
│   ├── model/               # Domain models (Route, Alert)
│   ├── dto/                 # Request/Response DTOs
│   └── exception/           # Global exception handling
├── src/main/resources/
│   ├── application.properties
│   └── data/                # Static route data (JSON)
├── Dockerfile               # Multi-stage Docker build
├── .github/workflows/
│   └── deploy.yml           # CI/CD: GitHub Actions → Render
└── pom.xml
```

---

## CI/CD

Pushing to the `main` branch automatically:

1. Creates a **GitHub Deployment** marked as *in progress*.
2. Triggers a **Render deploy hook**.
3. Polls the Render API every 30 seconds (up to 10 minutes) until the deployment is `live`.
4. Updates the GitHub Deployment status to **success** or **failure**.

Required GitHub repository secrets:

| Secret | Description |
|---|---|
| `GH_TOKEN` | GitHub personal access token (for deployment status updates) |
| `RENDER_DEPLOY_HOOK_URL` | Render deploy hook URL |
| `RENDER_API_KEY` | Render API key (for polling deploy status) |
| `RENDER_SERVICE_ID` | Render service ID |

---

## Maven Commands

| Command | Description |
|---|---|
| `mvn clean install` | Compile, test, and package the project |
| `mvn spring-boot:run` | Start the application in development mode |
| `mvn package -DskipTests` | Build the fat JAR without running tests |
| `mvn test` | Run unit tests |