# AI Log Debugger — Hybrid RAG Pipeline on Azure

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Azure](https://img.shields.io/badge/Azure_App_Service-0089D6?style=for-the-badge&logo=microsoft-azure&logoColor=white)
![OpenAI](https://img.shields.io/badge/Azure_OpenAI_GPT--4o--mini-412991?style=for-the-badge&logo=openai&logoColor=white)
![Redis](https://img.shields.io/badge/Azure_Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Status](https://img.shields.io/badge/Live-✅_Deployed-brightgreen?style=for-the-badge)

> **Production-grade AI backend** that automatically analyzes application logs and returns root cause, explanation, suggested fix, and confidence score — powered entirely by Azure AI services.
>
> Built with a **cost-optimized 9-step Hybrid RAG Pipeline**: Redis cache → Rule Engine → Vector Search → LLM. The LLM is called only when every other layer misses — minimizing token cost without sacrificing accuracy.

---

## 🔴 Live Demo

| Resource | URL |
|---|---|
| **API Endpoint** | `https://ai-log-debugger-app.azurewebsites.net/logs/analyze` |
| **Swagger UI** | `https://ai-log-debugger-app.azurewebsites.net/swagger-ui.html` |

**Try it now — no auth required:**
```bash
curl -X POST https://ai-log-debugger-app.azurewebsites.net/logs/analyze \
  -H "Content-Type: application/json" \
  -d '{"log": "ERROR: connection refused while connecting to database at localhost:5432"}'
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

## Why This Project Is Different

Most AI projects call the LLM directly for every request. This one doesn't.

This system is engineered around **cost control and latency reduction** — the two things that matter most in production AI systems. The LLM is the last resort, not the first call.

| What most AI projects do | What this project does |
|---|---|
| Call LLM for every request | Try cache → rules → vector search first |
| No cost awareness | Each layer tracked and optimized |
| Mock embeddings | Real Azure OpenAI ada-002 (1536 dims) |
| Local dev only | Fully deployed on Azure App Service |
| Single-layer retrieval | 9-step hybrid pipeline with fallbacks |

---

## Architecture — 9-Step Hybrid RAG Pipeline

```
Log Input
    ↓
1. Input Validation + Sanitization      ← reject malformed input early
    ↓
2. Normalize (trim + lowercase)         ← consistent cache keys
    ↓
3. Redis Cache Check    ──────── HIT → return instantly (zero AI cost)
    ↓ MISS
4. Rule Engine          ──────── HIT → keyword/semantic match (no LLM)
    ↓ MISS
5. Azure OpenAI Embeddings              ← ada-002, 1536-dim vector
    ↓
6. Azure AI Search (HNSW)              ← top-3 similar past logs
    ↓
7. Context Builder                     ← inject retrieved logs into prompt
    ↓
8. Azure OpenAI GPT-4o-mini ─── LAST RESORT: only when all layers miss
    ↓
9. Save to Vector Store + Redis Cache → Return JSON
```

**Result:** Repeated and pattern-matched logs cost near zero. The system gets smarter with every LLM call (self-improving via vector storage).

---

## Azure Infrastructure

All services are live and connected. Screenshots from the Azure portal:

### Azure OpenAI — GPT-4o-mini + text-embedding-ada-002
![Azure OpenAI](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-openai.png)

### Model Deployments — Both Active
![Model Deployments](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-deployments.png)

### Azure AI Search — Vector Index (HNSW, 1536 dims)
![Azure AI Search](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-search.png)

### Azure Cache for Redis — Running (SSL, port 6380)
![Azure Redis](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-redis.png)

### Azure App Service — Status: Running
![Azure App Service](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/azure-appservice.png)

---

## API Responses — All Three Pipeline Paths

### Path 1: Rule Engine Hit (confidence: 1.0, zero LLM cost)
![Rule Engine Response](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/response-rule-engine.png)

### Path 2: LLM Response — GPT-4o-mini (unknown error pattern)
![LLM Response](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/response-llm.png)

### Path 3: Stack Trace Input (multi-line supported)
![Stack Trace Response](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/response-stacktrace.png)

---

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Framework | Spring Boot 3.3.2, Java 17 | Backend API |
| LLM | Azure OpenAI GPT-4o-mini | Root cause generation |
| Embeddings | Azure OpenAI text-embedding-ada-002 | 1536-dim vector conversion |
| Vector Store | Azure AI Search (HNSW) | Semantic similarity retrieval |
| Cache | Azure Cache for Redis (SSL, 6hr TTL) | Zero-cost repeated log resolution |
| API Docs | Swagger UI (springdoc) | Live interactive documentation |
| Deployment | Azure App Service (Linux, Java 17 SE) | Production hosting |
| Build | Maven | Dependency management |

---

## Cost Optimization Strategy

Every layer is designed to short-circuit before reaching the LLM:

| Layer | Token Cost | Trigger Condition |
|---|---|---|
| Redis Cache | **Zero** | Same log seen before (SHA-256 key match) |
| Rule Engine — keyword | **Zero** | Known error pattern match |
| Rule Engine — semantic | Embedding only | Cosine similarity to known patterns |
| Vector Search | Embedding only | Similar past logs exist in index |
| LLM — GPT-4o-mini | Full cost | Unknown error, no matches found |

In production, the vast majority of repeated logs are served from cache at **zero AI cost**.

---

## Swagger UI

![Swagger UI](https://raw.githubusercontent.com/Sukesh-blip/AI-Log-Debugger/main/docs/screenshots/swagger-ui.png)

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
│   └── LogController.java            # REST endpoints + Swagger docs
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
│   └── GlobalExceptionHandler.java   # Structured error responses
│
└── util/
    └── HashUtil.java                 # SHA-256 cache key generator

src/main/resources/
├── application.properties            # Azure config
├── application-secrets.properties    # Keys (gitignored)
└── errors.json                       # 10-pattern knowledge base
```

---

## Running Locally

**Prerequisites:** Java 17, Maven, Azure subscription (OpenAI + AI Search + Redis)

```bash
# 1. Clone
git clone https://github.com/Sukesh-blip/AI-Log-Debugger.git
cd AI-Log-Debugger

# 2. Add secrets (gitignored)
# Create: src/main/resources/application-secrets.properties
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
AZURE_OPENAI_KEY=your-key
AZURE_SEARCH_ENDPOINT=https://your-search.search.windows.net
AZURE_SEARCH_KEY=your-admin-key
AZURE_REDIS_HOST=your-cache.redis.cache.windows.net
AZURE_REDIS_PASSWORD=your-redis-key

# 3. Run
./mvnw spring-boot:run
# On first run: vector index 'log-vectors' is created automatically

# 4. Test
curl -X POST http://localhost:8080/logs/analyze \
  -H "Content-Type: application/json" \
  -d '{"log": "java.lang.NullPointerException at com.example.Service.process(Service.java:42)"}'

# 5. Swagger
open http://localhost:8080/swagger-ui.html
```

---

## Deploy to Azure App Service

```bash
# Build
./mvnw clean package -DskipTests

# Deploy
az webapp deploy \
  --name your-app-name \
  --resource-group your-rg \
  --src-path target/ai-log-debugger-0.0.1-SNAPSHOT.jar \
  --type jar
```

---

## API Reference

### `POST /logs/analyze`
Accepts a single line, full stack trace, or raw console output.

```json
// Request
{ "log": "your error log here" }

// Response
{
  "rootCause": "string",
  "explanation": "string",
  "suggestedFix": "string",
  "confidence": 0.95
}
```

### `GET /logs/test-ai`
Quick smoke test — runs analysis on a hardcoded DB connection error. Useful for verifying Azure connectivity after deployment.

---

## Knowledge Base

The rule engine covers 10 common production error patterns without needing an LLM:

- Database connection failures
- `NullPointerException`
- `OutOfMemoryError`
- `StackOverflowError`
- JWT signature mismatch
- HTTP 403 / 404 errors
- `ClassNotFoundException`
- Duplicate key violations
- Connection timeouts
- SQL syntax errors

Unknown patterns fall through to GPT-4o-mini. Every LLM response is saved to the vector store — the system gets smarter over time.

---

## What This Project Demonstrates

| Engineering Decision | Why It Matters |
|---|---|
| Hybrid RAG Pipeline | Combines deterministic rules with probabilistic AI — best of both worlds |
| Cost-aware architecture | LLM is last resort, not default — production-ready thinking |
| Self-improving system | Every LLM call enriches the vector store for future retrieval |
| Redis caching with SHA-256 keys | Deterministic cache keys for identical log deduplication |
| Azure AI Search with HNSW | Approximate nearest-neighbor search for fast vector retrieval at scale |
| Auto-index creation on startup | Zero manual Azure portal setup — fully code-driven infrastructure |
| SSL Redis on port 6380 | Production-grade secure cache configuration |
| Structured JSON output with confidence score | Parseable, trustworthy responses for downstream consumers |

---

## Project Evolution

| Phase | Stack | What Was Built |
|---|---|---|
| Phase 1 | Spring Boot, Java, JSON | Rule engine, cosine similarity, mock embeddings |
| Phase 2 | + Groq LLM, Redis | Full RAG pipeline, rule engine, similarity threshold |
| Phase 3 | + Azure OpenAI, Azure AI Search, Azure App Service | Real embeddings (ada-002), HNSW vector store, GPT-4o-mini, production deployment |

---

## Author

**Sukesh Biradar** — Backend · Cloud · AI

[![GitHub](https://img.shields.io/badge/GitHub-Sukesh--blip-181717?style=flat&logo=github)](https://github.com/Sukesh-blip)

---

> This is not a tutorial project. It is a production-deployed, cost-optimized AI system built with real Azure infrastructure — engineered to minimize LLM cost while maximizing diagnostic accuracy through a layered retrieval strategy.
