# AI Log Debugger (Phase 2 – Hybrid AI Pipeline)

AI Log Debugger is a backend system designed to analyze application logs and identify the root cause of errors, along with explanations and suggested fixes. The system follows a cost-optimized hybrid AI architecture that combines rule-based logic, similarity-based retrieval, and LLM fallback to ensure accuracy, performance, and efficiency.

## Project Overview

Debugging production logs is often repetitive and time-consuming. This system automates that process by analyzing logs, identifying patterns, retrieving similar issues, and generating structured responses. Instead of relying entirely on LLMs, the system uses a layered approach to minimize unnecessary AI usage while maintaining high-quality outputs.

Given a log input, the system returns:
- Root cause  
- Explanation  
- Suggested fix  
- Confidence score  

The goal is to build a system that can understand variations in logs and map them to relevant issues using retrieval and controlled AI reasoning.

## Current Architecture (Hybrid AI Pipeline)

Raw Log → Log Normalization → Redis Cache → Rule Engine → Embedding Generation → Vector Search (Top-K) → Context Builder → LLM (Fallback) → Structured JSON Response

## Key Features

The system uses a hybrid AI pipeline where multiple layers handle different responsibilities. The rule engine handles common deterministic errors, the cache avoids repeated computation, vector search retrieves similar issues, and the LLM is used only when necessary. This reduces cost while improving response accuracy.

Similarity-based retrieval forms the foundation of the RAG approach. Logs are converted into vector representations and cosine similarity is used to find related issues. The system retrieves top-K similar logs and uses them to build context for further reasoning.

A critical improvement introduced in this phase is similarity threshold filtering. During testing, unrelated logs such as CORS errors were incorrectly matching database errors. This was resolved by applying a similarity threshold so that only high-confidence matches are considered. If no relevant match is found, the system falls back to the LLM.

The vector store is currently implemented in-memory and stores logs along with their embeddings. This allows fast similarity search and is designed to be replaced later with a production-grade vector database such as Azure AI Search or FAISS.

The system produces structured JSON responses containing root cause, explanation, suggested fix, and a confidence score.

## Example

Input:
{
  "log": "ERROR: connection refused while connecting to database"
}

Output:
{
  "rootCause": "Database connection failure",
  "explanation": "The application is unable to connect to the database service",
  "suggestedFix": "Check DB service availability and connection configuration",
  "confidence": 0.89
}

## Engineering Learnings

One key realization during development was that the top similarity result is not always correct. Without filtering, the system may return irrelevant matches. Introducing a similarity threshold significantly improved accuracy by eliminating low-confidence results.

Another important learning was that embedding quality directly impacts system performance. Mock embeddings led to incorrect matches, highlighting the need for real embedding models.

The system also demonstrates that retrieval alone is not sufficient. It must be controlled. Proper filtering, routing, and fallback mechanisms are required to ensure correct outputs.

A core design principle followed is that LLM should be used as a last resort. Most requests should be handled through rules, cache, or retrieval to optimize cost and latency.

## Cost Optimization Strategy

The system minimizes LLM usage through multiple layers. The cache avoids repeated processing, the rule engine handles predictable errors, vector search reduces dependency on LLM by retrieving known patterns, and similarity thresholds prevent unnecessary calls. The LLM is invoked only when no reliable match is found.

## Project Structure

com.maveric.ailogger

controller → REST API endpoints  
service → Business logic including embedding, similarity, vector search, and analysis  
model → Data models  
dto → Request and response objects  
cache → Redis caching layer  
rules → Rule engine implementation  
vector → Vector store and search logic  
resources → Knowledge base (JSON)

## Tech Stack

Java  
Spring Boot  
Redis  
Groq LLM (openai/gpt-oss-120b)  
Cosine Similarity  
Vector-based Retrieval  
REST APIs  

## How to Run

Clone the repository and run the Spring Boot application. Send a POST request to:

http://localhost:8080/logs/analyze

Request body:
{
  "log": "your log here"
}

## Current Status

The backend API is complete. Redis caching is implemented and working. The rule engine handles common error patterns. A basic RAG pipeline with embedding and vector search has been implemented. Similarity threshold filtering has been added to improve accuracy and prevent incorrect matches.

## Next Steps

The next phase will focus on replacing mock embeddings with real embedding models, integrating a vector database such as Azure AI Search, improving similarity-based routing, enhancing context building, adding observability, and deploying the system on cloud infrastructure.

## Why This Project Stands Out

Most beginner projects focus on CRUD operations or static logic. This project demonstrates backend system design, AI integration, cost-aware architecture, and retrieval-based reasoning. It reflects how real-world AI-backed systems are designed and optimized.

## Author

Maveric (Sukesh Biradar)  
Backend | Cloud | AI (building real systems)

## Final Note

This project is not just an AI feature implementation. It is a controlled AI system designed to balance accuracy, cost, and performance through structured engineering decisions.


-----------------------------------------------------------------------------------------------------------------------------------------------------------------

# AI Log Debugger (Phase 1)

A backend system designed to analyze application logs and identify the root cause of errors, along with explanations and suggested fixes.

---

## Project Overview

Debugging logs is often time-consuming and repetitive. This project aims to simplify that process by automatically analyzing logs and providing structured insights.

Given a log input, the system returns:

* Root cause
* Explanation
* Suggested fix
* Confidence score

The goal is not just to match errors, but to build a system that can **understand variations in logs** and map them to relevant issues.

---

## Current Status

Phase 1 focuses on building a strong backend and retrieval foundation before integrating AI.

So far, the system includes:

* A Spring Boot REST API for log analysis
* Log ingestion and basic normalization
* A JSON-based knowledge base of error patterns
* A similarity-based matching engine
* Cosine similarity for comparing logs
* A simple embedding simulation (word frequency based)

This allows the system to move beyond exact keyword matching and handle variations in log inputs.

---

## Architecture (Current Implementation)

The current flow of the system is:

Client
→ Spring Boot API (`/logs/analyze`)
→ Log Ingestion
→ Log Analysis Service
→ Embedding Simulation
→ Similarity Engine (Cosine Similarity)
→ Knowledge Base (JSON)
→ Response Formatter

A complete high-level architecture diagram (RAG-based design) is included below.

---

## Key Features

### 1. Log Analysis

The system identifies common categories of errors such as:

* SQL-related issues
* Null pointer exceptions
* JWT-related errors

---

### 2. Data-Driven Knowledge Base

Error patterns are stored externally in a JSON file.
This makes the system easy to extend without modifying code.

---

### 3. Similarity-Based Matching

Instead of relying on exact matches:

* Logs are converted into vector-like representations
* Cosine similarity is used to find the closest match
* Different variations of similar errors can still be identified

---

### 4. Embedding Simulation

A simple embedding approach based on word frequency is used to simulate vector representations.

This is designed to prepare the system for:

* Real embeddings (OpenAI / Azure)
* Vector database integration

---

## Example

### Input

```json
{
  "log": "database column missing error"
}
```

### Output

```json
{
  "rootCause": "Database schema mismatch",
  "explanation": "Column not found in database",
  "suggestedFix": "Check schema and add missing column",
  "confidence": 0.87
}
```

---

## Project Structure

```
com.maveric.ailogger
│
├── controller       -> API endpoints
├── service          -> Business logic
├── model            -> Data models
├── dto              -> Request/Response objects
├── resources        -> errors.json (knowledge base)
```

---

## What Makes This Different

Most beginner projects focus on CRUD operations or static logic.

This project is designed to reflect a more realistic system:

* Data-driven architecture
* Similarity-based reasoning
* Clear separation of concerns
* Structured for future AI integration (RAG pipeline)

---

## Next Steps

The next phase will focus on moving toward a complete RAG-based system:

* Implement Top-K retrieval
* Introduce a vector database (FAISS / ChromaDB)
* Add prompt construction
* Integrate an LLM for better explanations
* Deploy the system using Docker and cloud services

---

## Tech Stack

* Java
* Spring Boot
* REST APIs
* JSON (Knowledge Base)
* Cosine Similarity
* Basic Vector Modeling

---

## How to Run

1. Clone the repository
2. Run the Spring Boot application
3. Send a request using Postman or any API client

```
POST http://localhost:8080/logs/analyze
```

### Request Body

```json
{
  "log": "your log here"
}
```

---

## Learning Outcome

This project helped in understanding:

* Backend system design
* Data-driven architecture
* Basics of retrieval systems
* How RAG pipelines are structured
* Preparing a system for AI integration

---

## Status

Phase 1 complete.
Currently working toward full AI-powered log analysis using a RAG-based approach.

---

## Author

Maveric
Backend | Cloud | AI (learning and building)

Here is the HLD diagram for this project
![image](https://github.com/Sukesh-blip/AI-Log-Debugger/blob/32c739322d74faff0905866879049c9679d7a796/AI-LogDebugger%20HLD.png)

## 👨‍💻 Author

Sukesh Biradar
