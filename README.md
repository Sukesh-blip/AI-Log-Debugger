# AI Log Debugger

AI Log Debugger is an intelligent service built with Spring Boot that analyzes software logs, identifies root causes, and provides actionable solutions. It simplifies the debugging process by automatically interpreting error traces and returning detailed, structured findings with confidence scores.

## Features
- **Intelligent Log Analysis**: Submits application logs to be analyzed and decoded.
- **Root Cause & Fix Recommendation**: Returns precise root cause analysis and a suggested fix for each error.
- **RESTful API**: Easily integrate with other services or front-end applications via the `/logs/analyze` endpoint.
- **Spring Boot Architecture**: Built robustly, maintaining modern architectural standards with decoupled DTOs, Controllers, and Services.

## Tech Stack
- **Java 17**
- **Spring Boot 3.3**
- **Maven**

## Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6+ (or use the included wrapper)

### Running the Application Locally
1. Clone this repository:
   ```bash
   git clone <your-repository-url>
   ```
2. Navigate into the project directory:
   ```bash
   cd AI-Log-Debugger-1
   ```
3. Build and run via Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   Or on Windows:
   ```cmd
   mvnw.cmd spring-boot:run
   ```

### API Usage
The main endpoint available is `/logs/analyze`. You can send a POST request with the log text in a JSON body.

**Endpoint:**
`POST http://localhost:8080/logs/analyze`

**Request Body:**
```json
{
  "log": "ERROR: Column 'updated_at' not found in table 'users'"
}
```

**Response:**
```json
{
  "rootCause": "Database schema mismatch",
  "explanation": "Column 'updated_at' not found in table",
  "suggestedFix": "Add column using ALTER TABLE",
  "confidence": 0.85
}
```

## IDE Setup (Spring Tool Suite / Eclipse)
This project features standard Java code and does not rely on Lombok (which has been manually removed to ensure maximum compatibility out-of-the-box). You can safely import this project as an **Existing Maven Project** into STS or Eclipse without needing additional IDE plugins.
