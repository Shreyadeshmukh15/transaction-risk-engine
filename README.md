
# Transaction Risk Engine

A RESTful backend application built using Java and Spring Boot to
process financial transactions, calculate risk scores, classify
transactions by risk level, and flag potentially risky transactions.

## Features

- Create and store transactions
- Rule-based transaction risk scoring
- Risk classification: LOW, MEDIUM, HIGH
- Automatic flagging of high-risk transactions
- Retrieve transactions by ID
- Filter transactions by risk level
- Pagination and sorting
- Request validation and structured error responses
- Unit and controller tests

## Technology Stack

- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA
- MySQL 8
- Maven
- JUnit 5
- Mockito
- MockMvc
- Swagger / OpenAPI

## Risk Scoring Rules

| Rule | Score |
|---|---:|
| Transaction amount >= 50,000 | +40 |
| Channel is ONLINE | +20 |
| International transaction | +30 |

Risk classification:
- LOW: score below 30
- MEDIUM: score from 30 to 59
- HIGH: score 60 or above

Transactions classified as HIGH are flagged.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions` | Create a transaction |
| GET | `/api/transactions/{id}` | Get a transaction by ID |
| GET | `/api/transactions` | List transactions |
| GET | `/api/transactions?riskLevel=HIGH` | Filter by risk |
| GET | `/api/transactions?page=0&size=10` | Paginated results |

## Sample Request

POST `/api/transactions`

```json
{
  "amount": 85000,
  "currency": "INR",
  "channel": "ONLINE",
  "international": true
}
```

Expected risk score: 90  
Expected risk level: HIGH  
Expected flagged status: true

## Setup Instructions

1. Install Java 17 and MySQL 8.
2. Create a database named `transaction_risk_db`.
3. Configure your database URL, username, and password in
   `src/main/resources/application.properties`.
4. Run the application:

```bash
mvnw.cmd spring-boot:run
```

## Run Tests

```bash
mvnw.cmd test
```

## Swagger API Documentation

After starting the application, open:

http://localhost:8080/swagger-ui/index.html

## Future Improvements

- Configurable risk-scoring rules
- Additional transaction validation
- Integration tests with a test database
- Authentication and authorization
