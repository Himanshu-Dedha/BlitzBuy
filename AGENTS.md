# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/example/blitzbuy/`: Spring Boot app code
  - `controller/` (REST endpoints), `service/` (+ `impl/`), `repository/`, `data/entity/`, `data/enums/`
- `src/main/resources/`: config (e.g., `application.yml`)
- `src/test/java/`: unit/integration tests mirroring main package paths
- Root: `pom.xml` (Maven), `docker-compose.yml` (Postgres, Redis)

## Build, Test, and Development Commands
- `./mvnw clean package`: compile + run tests + build JAR
- `./mvnw spring-boot:run`: run API at `http://localhost:8080/api/engine/`
- `./mvnw test`: run unit/integration tests
- `docker compose up -d postgres redis`: start local dependencies
- DB override example: `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/blitzbuy_db \`
  `SPRING_DATASOURCE_USERNAME=admin SPRING_DATASOURCE_PASSWORD=admin ./mvnw spring-boot:run`

## Coding Style & Naming Conventions
- Java 17, 4-space indents, UTF-8. Packages lower-case dot notation.
- Classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE_CASE`.
- REST paths: plural nouns; keep controller names `<Resource>Controller`.
- Use Lombok (`@Data`, etc.) consistently; avoid business logic in entities.

## Testing Guidelines
- Framework: JUnit 5 with Spring Boot Test.
- Place tests under `src/test/java` mirroring packages; suffix with `Tests` (e.g., `OrderServiceImplTests`).
- Use `@DataJpaTest` for repositories, `@WebMvcTest` for controllers, `@SpringBootTest` for full context.
- Run: `./mvnw test`. Aim for meaningful coverage on services and controller slices.

## Commit & Pull Request Guidelines
- Commits: small, focused, imperative (e.g., "Add order status enum"). Reference issues (`#123`) when relevant.
- PRs: clear description, rationale, test plan (commands/cURLs), and screenshots/logs if UI/HTTP behavior changes.
- Ensure build and tests pass locally before requesting review.

## Security & Configuration Tips
- Local DB via compose: Postgres on `localhost:5433`, DB `blitzbuy_db`, user/pass `admin`.
- For local dev without Flyway, set `spring.jpa.hibernate.ddl-auto=update` or provide `schema.sql`.
- Keep secrets out of VCS; prefer env vars or an ignored `application-local.yml`.
