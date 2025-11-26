# GEMINI.md

## Project Overview

This is a Java Spring Boot application named "BlitzBuy". Based on the file structure and content, it appears to be an e-commerce or online shopping application.

**Key Technologies:**
*   **Backend:** Java 17, Spring Boot
*   **Data:** Spring Data JPA, PostgreSQL
*   **Build:** Apache Maven
*   **Containerization:** Docker (with services for PostgreSQL and Redis)
*   **API:** RESTful API exposed under the `/api/engine` context path.

**Architecture:**
The application follows a standard layered architecture for a Spring Boot project:
*   `controller`: Defines REST API endpoints.
*   `service`: Contains the business logic.
*   `repository`: Manages data access using Spring Data JPA.
*   `data/entity`: Defines the database models.

## Building and Running

### Using Maven
To build and run the application directly using the Maven wrapper:

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will be accessible at `http://localhost:8080/api/engine`.

### Using Docker
The `docker-compose.yml` file defines the necessary database (PostgreSQL) and caching (Redis) services.

To start these services:
```bash
docker-compose up -d
```
After starting the services, you can run the application using your IDE or the Maven command above. The application is configured in `application.yml` to connect to the PostgreSQL instance defined in the Docker Compose file.

## Development Conventions

*   **REST APIs:** The project exposes RESTful APIs. For example, the `ProductController` handles `v1/products` endpoints.
*   **Lombok:** The project uses `lombok` to reduce boilerplate code (e.g., `@RequiredArgsConstructor`).
*   **JPA:** Database entities are managed with Jakarta Persistence (JPA) and Hibernate.
*   **Testing:** Tests are located in `src/test/java` and can be run using Maven:
    ```bash
    ./mvnw test
    ```
