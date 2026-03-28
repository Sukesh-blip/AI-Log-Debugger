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
