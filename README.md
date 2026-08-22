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
| Tracing & Observability | Micrometer Tracing (Brave) + Zipkin UI |
| API Documentation | Springdoc OpenAPI (Swagger UI) |
| Rate Limiting | Bucket4j |
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
   cd transit-flow-backend
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

- **API Documentation (Swagger UI)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Running with Docker

```bash
docker build -t transit-flow .
docker run -p 8080:8080 -e GEMINI_API_KEY=your_api_key_here transit-flow
```

---

## Distributed Tracing & Observability (Zipkin UI)

TransitFlow uses **Micrometer Tracing** with the **Brave** bridge and **SLF4J MDC** to trace every request, spotting submission, and AI route extraction end-to-end.

### How It Works
- **Automatic Trace & Span IDs**: Every incoming HTTP request is assigned a unique `traceId` and `spanId`.
- **MDC Correlation**: All backend log lines are automatically tagged with `[transitflow-sprint1,<traceId>,<spanId>]`.
- **Response Headers**: The active `traceId` is returned in the `X-Request-ID` HTTP response header for client-side correlation.
- **Visual Dashboard**: Traces and latency waterfalls are automatically reported to Zipkin.

---

### Step-by-Step Instructions to View Traces in Zipkin UI

#### Step 1: Start the Zipkin UI Server

You can run Zipkin either using the standalone Java JAR or Docker:

**Option A - Run with Java (No Docker required):**
```bash
# If you don't already have zipkin.jar, download it:
curl -sSL https://zipkin.io/quickstart.sh | bash -s

# Run the Zipkin server:
java -jar zipkin.jar
```

**Option B - Run with Docker:**
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

Zipkin will be listening on **[http://localhost:9411](http://localhost:9411)**.

---

#### Step 2: Start the Backend Application
In a separate terminal, start TransitFlow:
```bash
export GEMINI_API_KEY=your_api_key_here
mvn spring-boot:run
```

---

#### Step 3: Trigger an API Request
Send a request to any endpoint, for example, a bus spotting:
```bash
curl -X POST http://localhost:8080/api/spottings \
  -H "Content-Type: application/json" \
  -i \
  -d '{"routeNumber":"36H","location":"UKKADAM","mismatch":true}'
```
Notice the `X-Request-ID` in the response headers:
```http
HTTP/1.1 201 Created
X-Request-ID: 6a82025575fc96bf9b32dd20ae37887e
Content-Type: application/json
```

---

#### Step 4: Open the Zipkin Tracing Dashboard
1. Open your browser and navigate to **[http://localhost:9411](http://localhost:9411)**.
2. Click the **"Run Query"** button on the search page to view recent request traces.
3. Or paste your `X-Request-ID` / `traceId` directly into the search bar.
4. Click on any trace to inspect:
   - Visual execution timelines (waterfall chart).
   - Controller and service layer duration.
   - HTTP status codes, method, path, and error tags.

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