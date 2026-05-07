# AI Log Debugger — Phase 3 (Azure AI Stack)

> A production-grade backend system that automatically analyzes application logs and returns root cause, explanation, suggested fix, and confidence score — powered entirely by Azure AI services.

---

## Live Demo

**API Endpoint:** `https://ai-log-debugger-app.azurewebsites.net/logs/analyze`

**Swagger UI:** `https://ai-log-debugger-app.azurewebsites.net/swagger-ui.html`

---

## What It Does

Debugging production logs is repetitive and time-consuming. This system automates it.

Send any log — a single error line, a full stack trace, or raw console output — and the system returns:

- **Root Cause** — what went wrong
- **Explanation** — why it happened
- **Suggested Fix** — how to resolve it
- **Confidence Score** — how certain the system is (0.0 to 1.0)

### Example

**Request:**
```json
POST /logs/analyze
{
  "log": "ERROR: connection refused while connecting to database at localhost:5432"
}
```

**Response:**
```json
{
  "rootCause": "Database connection failed",
  "explanation": "Connection refused — the target service is not running or unreachable.",
  "suggestedFix": "Check DB service status, host, and port configuration.",
  "confidence": 1.0
}
```

---

## Architecture — 9-Step Hybrid RAG Pipeline

```
Log Input
    ↓
1. Input Validation + Sanitization
    ↓
2. Normalize (trim + lowercase)
    ↓
3. Redis Cache Check          → HIT: return instantly, zero AI cost
    ↓
4. Rule Engine                → HIT: keyword/semantic match, no LLM needed
    ↓
5. Azure OpenAI Embeddings    → convert log to 1536-dim vector (ada-002)
    ↓
6. Azure AI Search (HNSW)     → find top-3 similar past logs
    ↓
7. Context Builder            → join similar logs into prompt context
    ↓
8. Azure OpenAI GPT-4o-mini   → only called when all above layers miss
    ↓
9. Save to Vector Store + Cache → Return JSON
```

This is a **cost-optimized Hybrid RAG Pipeline**. The LLM is the last resort — most requests are resolved through cache, rules, or vector retrieval without ever calling the LLM.

---

## Swagger UI

![Swagger UI](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/swagger-ui.png)

---

## API Responses

### Rule Engine Response (confidence: 1.0 — instant, no LLM cost)

![Rule Engine Response](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/response-rule-engine.png)

### LLM Response — GPT-4o-mini (unknown error pattern)

![LLM Response](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/response-llm.png)

### Stack Trace Input (multi-line raw input supported)

![Stack Trace Response](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/response-stacktrace.png)

---

## Azure Infrastructure

### Azure OpenAI — GPT-4o-mini + text-embedding-ada-002

![Azure OpenAI](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-openai.png)

### Model Deployments — Both Succeeded

![Model Deployments](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-deployments.png)

### Azure AI Search — Vector Store (HNSW, 1536 dims)

![Azure AI Search](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-search.png)

### Azure Cache for Redis — Running

![Azure Redis](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-redis.png)

### Azure App Service — Status: Running

![Azure App Service](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-appservice.png)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3.2, Java 17 |
| LLM | Azure OpenAI GPT-4o-mini |
| Embeddings | Azure OpenAI text-embedding-ada-002 (1536 dims) |
| Vector Store | Azure AI Search (HNSW algorithm) |
| Cache | Azure Cache for Redis (SSL, port 6380, 6hr TTL) |
| API Docs | Swagger UI (springdoc) |
| Deployment | Azure App Service (Linux, Java 17 SE) |
| Build | Maven |

---

## Cost Optimization Strategy

The pipeline short-circuits as early as possible to minimize Azure OpenAI token usage:

| Layer | Cost | When triggered |
|---|---|---|
| Redis Cache | Zero | Same log seen before |
| Rule Engine (keyword) | Zero | Known error patterns |
| Rule Engine (semantic) | Embedding only | Similar to known patterns |
| Vector Search | Embedding only | Similar past logs exist |
| LLM (GPT-4o-mini) | Full cost | Unknown error, no matches |

In production, the vast majority of repeated logs are served from cache with zero AI cost.

---

## Project Structure

```
src/main/java/com/example/demo/
│
├── config/
│   ├── AzureConfig.java              # OpenAI + Search + Redis beans
│   ├── RedisConfig.java              # Redis SSL template
│   └── SearchIndexInitializer.java   # Auto-creates vector index on startup
│
├── controller/
│   └── LogController.java            # REST endpoints + Swagger
│
├── service/
│   ├── LogAnalysisService.java       # Orchestrates the 9-step pipeline
│   ├── EmbeddingService.java         # Azure OpenAI ada-002 embeddings
│   ├── LLMService.java               # Azure OpenAI GPT-4o-mini
│   ├── VectorStore.java              # Azure AI Search save + search
│   ├── VectorSearchService.java      # Vector similarity search
│   ├── RuleEngineService.java        # Keyword + semantic rules
│   ├── CacheService.java             # Redis get/set with TTL
│   ├── SimilarityService.java        # Cosine similarity
│   └── KnowledgeBase.java            # Loads errors.json
│
├── model/
│   ├── LogSearchDocument.java        # Azure Search document schema
│   └── ErrorKnowledge.java           # Knowledge base entry
│
├── dto/
│   ├── LogRequest.java               # API request
│   ├── AnalysisResponse.java         # API response
│   └── VectorEntry.java              # Vector store entry
│
├── exception/
│   └── GlobalExceptionHandler.java   # Clean error responses
│
└── util/
    └── HashUtil.java                 # SHA-256 cache key generator

src/main/resources/
├── application.properties            # Azure config
├── application-secrets.properties    # Keys (not committed)
└── errors.json                       # 10 error pattern knowledge base
```

---

## Running Locally

**Prerequisites:** Java 17, Maven, Azure subscription

**1. Clone the repository:**
```bash
git clone https://github.com/Sukesh-blip/AI-Log-Debugger.git
cd AI-Log-Debugger
```

**2. Fill in your secrets:**

Create `src/main/resources/application-secrets.properties`:
```properties
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
AZURE_OPENAI_KEY=your-key

AZURE_SEARCH_ENDPOINT=https://your-search.search.windows.net
AZURE_SEARCH_KEY=your-admin-key

AZURE_REDIS_HOST=your-cache.redis.cache.windows.net
AZURE_REDIS_PASSWORD=your-redis-key
```

**3. Run:**
```bash
./mvnw spring-boot:run
```

On first startup you will see:
```
Index 'log-vectors' not found — creating...
Index 'log-vectors' created successfully.
```

The Azure AI Search index is created automatically — no manual portal setup needed.

**4. Test:**
```bash
POST http://localhost:8080/logs/analyze
Content-Type: application/json

{"log": "ERROR: connection refused while connecting to database at localhost:5432"}
```

**5. Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

---

## Deploying to Azure App Service

**Build:**
```bash
./mvnw clean package -DskipTests
```

**Deploy:**
```bash
az webapp deploy \
  --name your-app-name \
  --resource-group your-rg \
  --src-path target/ai-log-debugger-0.0.1-SNAPSHOT.jar \
  --type jar
```

---

## API Reference

### `POST /logs/analyze`

Accepts any log input — single line, full stack trace, or raw console output.

**Request:**
```json
{
  "log": "your error log here"
}
```

**Response:**
```json
{
  "rootCause": "string",
  "explanation": "string",
  "suggestedFix": "string",
  "confidence": 0.0
}
```

### `GET /logs/test-ai`

Quick test endpoint — runs analysis on a hardcoded DB connection error.

---

## Knowledge Base

The rule engine uses a built-in knowledge base of 10 common error patterns covering:

- Database connection failures
- NullPointerException
- OutOfMemoryError
- StackOverflowError
- JWT signature mismatch
- 403 / 404 HTTP errors
- ClassNotFoundException
- Duplicate key violations
- Connection timeouts
- SQL syntax errors

Any log not matching these patterns is handled by the LLM.

---

## What This Project Demonstrates

- **Hybrid RAG Pipeline** — combining deterministic rules with probabilistic AI
- **Cost-aware AI architecture** — LLM as last resort, not first call
- **Azure AI integration** — OpenAI, AI Search, Redis, App Service
- **Production patterns** — caching, fallbacks, input sanitization, structured output
- **Self-improving system** — every LLM response is stored for future vector retrieval

---

## Project Evolution

| Phase | What was built |
|---|---|
| Phase 1 | Spring Boot API, JSON knowledge base, cosine similarity, mock embeddings |
| Phase 2 | Full RAG pipeline, Redis cache, rule engine, similarity threshold, Groq LLM |
| Phase 3 | Azure OpenAI (real embeddings + GPT-4o-mini), Azure AI Search (HNSW vector store), Azure Cache for Redis (SSL), Azure App Service deployment |

---

## Author

**Sukesh Biradar**
Backend | Cloud | AI

GitHub: [Sukesh-blip](https://github.com/Sukesh-blip)

---

> This project is not just an AI feature implementation. It is a controlled AI system designed to balance accuracy, cost, and performance through structured engineering decisions.
