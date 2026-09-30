# Policy Management API

![Java](https://img.shields.io/badge/Java-21_LTS-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)

A robust, enterprise-grade RESTful API built with **Spring Boot 3** and **Java 21** to manage insurance policies and their associated risks. The service employs a layered architecture and asynchronous event-driven mechanisms for third-party integrations.

## 🚀 Features

- **Policy Lifecycle Management:** Create, list, renew, and cancel policies.
- **Risk Management:** Attach and manage specific risks (e.g., Fire, Liability) to collective policies.
- **Financial Calculations:** Automated IPC (Consumer Price Index) adjustments upon policy renewal.
- **Event-Driven Integration:** Asynchronous architecture to decouple internal logic from legacy CORE system notifications.
- **Security:** API Key-based access control via custom interceptors.
- **Observability:** JPA Auditing enabled for precise tracking of record creations and updates.

## 🏗️ Architecture & Patterns

- **Layered Architecture:** Clear separation of concerns (Controllers, Services, Repositories, Models).
- **DTO Pattern:** Entities are strictly isolated from the presentation layer to avoid data leakage and `LazyInitializationException` issues.
- **Asynchronous Processing (`@Async`):** Integration with the legacy CORE system is handled asynchronously using Spring's `ApplicationEventPublisher`, ensuring non-blocking HTTP responses.
- **Global Exception Handling:** Centralized `@ControllerAdvice` to gracefully handle business rule violations and return standard JSON errors.

## 🛠️ Tech Stack

- **Core:** Java 21 LTS, Spring Boot 3.3.4
- **Persistence:** Spring Data JPA, H2 In-Memory Database
- **Documentation:** OpenAPI 3 / Swagger UI
- **Tooling:** Maven Wrapper, Lombok

## ⚙️ Getting Started

### Prerequisites
- JDK 21+ installed on your machine.
- Maven (Optional, the project includes `mvnw`).

### Running Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/polizas-api.git
   cd polizas-api
   ```

2. Start the application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

3. The server will start on `http://localhost:8080`.
   *Note: The in-memory database will be automatically seeded with sample data on startup.*

## 📚 API Documentation (Swagger)

This project integrates **Springdoc OpenAPI**. Once the application is running, you can interact with the API documentation through the Swagger UI:

👉 **[Swagger UI: http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

## 🔒 Security

All endpoints are secured. You must include the following header in your HTTP requests:
```http
api-key: 123456
```

## 🧪 Testing

The project includes unit tests for the core business logic (Services) and integration tests.
To run the test suite:
```bash
./mvnw test
```
