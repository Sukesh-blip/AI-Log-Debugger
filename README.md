# 🚀 AI Log Debugger (Phase 1 Complete)

> Transforming raw logs into actionable insights using intelligent backend + emerging AI architecture.

---

## 🧠 Project Overview

AI Log Debugger is a backend system that analyzes application logs and identifies the **root cause of errors**, along with explanations and suggested fixes.

Instead of manually scanning thousands of lines of logs, this system:

* Detects error patterns
* Matches similar issues
* Provides structured debugging insights

---

## ⚙️ Current Status (Phase 1)

✅ Backend API built
✅ Rule-based log analysis
✅ Data-driven knowledge base
✅ Similarity-based matching engine
✅ Embedding simulation (AI foundation)

---

## 🏗️ Architecture (Current Implementation)

```text
Client
  ↓
Spring Boot API (/logs/analyze)
  ↓
Log Analysis Service
  ↓
Embedding Simulation (word vector)
  ↓
Similarity Engine (cosine similarity)
  ↓
Knowledge Base (JSON)
  ↓
Response Formatter
```

---

## 🔥 Key Features

### 1. Intelligent Log Analysis

* Detects common errors:

  * SQL Errors
  * NullPointerException
  * JWT Issues

---

### 2. Knowledge-Driven System

* Error patterns stored in external JSON
* Easily extendable without code changes

---

### 3. Similarity-Based Matching

* Not limited to exact keywords
* Uses:

  * token matching
  * cosine similarity
* Handles variations in logs

---

### 4. Embedding Simulation

* Converts logs into vector form (word frequency)
* Implements **cosine similarity**
* Foundation for real AI integration

---

## 🧪 Example

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

## 📂 Project Structure

```
com.maveric.ailogger
│
├── controller       → API endpoints
├── service          → Business logic
├── model            → Data models
├── dto              → Request/Response
├── resources        → errors.json
```

---

## 🧠 What Makes This Project Unique

Most projects:
❌ Static logic
❌ Hardcoded rules
❌ No real-world relevance

This project:
✅ Data-driven
✅ Similarity-based reasoning
✅ RAG-ready architecture
✅ Designed like a real production system

---

## 📈 What’s Coming Next (Phase 2)

* 🔜 Top-K retrieval (multiple matches)
* 🔜 Vector database integration
* 🔜 Real embeddings (OpenAI/Azure)
* 🔜 LLM-based explanation generation
* 🔜 Cloud deployment (Docker + Azure)

---

## 🎯 Tech Stack

* Java
* Spring Boot
* REST APIs
* JSON (Knowledge Base)
* Cosine Similarity
* Basic Vector Modeling

---

## 🚀 How to Run

1. Clone repository
2. Run Spring Boot application
3. Use Postman:

```
POST http://localhost:8080/logs/analyze
```

Body:

```json
{
  "log": "your log here"
}
```

---

## 💡 Learning Outcome

This project demonstrates:

* Backend system design
* Data-driven architecture
* Introduction to RAG systems
* AI integration readiness
* Real-world debugging workflow

---

## ⚔️ Status

> Phase 1 Complete — Moving toward full AI-powered RAG system.

---

## 👨‍💻 Author

Maveric — Backend + Cloud + AI Enthusiast 🚀
