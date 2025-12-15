
---

```markdown
# ⚡ BlitzBuy: High-Throughput Flash Sale Engine

> **An Event-Driven, Scalable Backend System designed to handle massive traffic spikes with zero overselling and high availability.**

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-green?style=for-the-badge&logo=spring)
![Redis](https://img.shields.io/badge/Redis-Caching_%26_Locking-red?style=for-the-badge&logo=redis)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-black?style=for-the-badge&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Persistence-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED?style=for-the-badge&logo=docker)

## 📖 Overview
BlitzBuy is a backend engine engineered to solve the **"Flash Sale Problem"**: Selling a limited inventory (e.g., 100 iPhones) to 100,000 concurrent users without:
1.  **Overselling** (Race Conditions).
2.  **Database Crashes** (Connection saturation).
3.  **High Latency** (Blocking I/O).

Traditional RDBMS transactions (`SELECT FOR UPDATE`) provide consistency but kill throughput. BlitzBuy moves the "State" to **Redis** for speed and uses **Apache Kafka** for "Peak Load Leveling," ensuring the database writes happen at a sustainable pace.

---

## 🏗 Architecture
The system employs an **Event-Driven Architecture** to decouple the User Experience (latency-sensitive) from Data Persistence (throughput-sensitive).

```mermaid
graph TD
    User(User Client) -->|HTTP POST| LoadBalancer[API Gateway / Controller]
    
    subgraph "Synchronous Hot Path (latency < 50ms)"
        LoadBalancer -->|1. Rate Limit Check| Interceptor[Rate Limit Interceptor]
        Interceptor -->|2. Check Idempotency| Redis[(Redis Cache)]
        Interceptor -->|3. Atomic Decrement (DECR)| Redis
    end
    
    subgraph "Asynchronous Cold Path (Guaranteed Delivery)"
        Redis -- Inventory Success --> Producer[Kafka Producer]
        Producer -->|4. Publish Event| Kafka{Apache Kafka}
        Kafka -->|5. Consume Batch| Consumer[Order Consumer Service]
        Consumer -->|6. Persist| DB[(PostgreSQL)]
        Consumer -- Failure --> DLQ[Dead Letter Queue]
    end

    User <.. 202 Accepted .. Producer
```

---

## 🚀 Key Engineering Challenges & Solutions

### 1. Concurrency & The "Lost Update" Anomaly
*   **The Problem:** Under high concurrency (20+ threads), a standard "Read-Modify-Write" database transaction causes race conditions, leading to massive overselling.
*   **The Initial Fix:** Implemented **Pessimistic Locking** (`SELECT ... FOR UPDATE`) in Postgres. While accurate, it serialized requests, destroying throughput.
*   **The Final Solution:** Migrated to **Redis Atomic Counters**. By using the `DECR` operation, the inventory decrement happens in a single atomic step in memory, acting as a high-speed semaphore.

### 2. Peak Load Leveling
*   **The Problem:** Direct database writes cannot scale to 10k+ TPS on a standard instance; connection pools become the bottleneck.
*   **The Solution:** Implemented the **Write-Behind Pattern**. The API acknowledges the request immediately after the Redis check and pushes an event to **Kafka**. A background consumer drains the topic at a controlled rate, preventing database overwhelm.

### 3. Distributed Resilience
*   **Idempotency:** Implemented `Idempotency-Key` tracking in Redis to prevent duplicate orders during network retries (e.g., client clicks "Buy" twice).
*   **Rate Limiting:** Built a custom `HandlerInterceptor` using Redis Sliding Windows to cap users at 5 req/min, preventing abuse.
*   **Fault Tolerance:** Configured **Exponential Backoff** for the Kafka Consumer. If the Database goes down, messages retry (1s -> 2s -> 4s) before moving to a **Dead Letter Queue (DLQ)**, ensuring zero data loss.

---

## 📊 Performance Benchmarks
Load testing performed using **K6** on local development hardware (Windows + Docker via WSL2).

| Metric | Result | Notes |
| :--- | :--- | :--- |
| **Throughput** | **1,778 TPS** | Sustained load over 30s |
| **Success Rate** | **100%** | Zero dropped orders under saturation |
| **Inventory Accuracy** | **100%** | Validated via Integration Tests |
| **P95 Latency** | ~500ms* | *High due to local Docker networking overhead; projected <50ms in cloud VPC.* |

---

## 🛠 Tech Stack & Tools
*   **Language:** Java 21, Spring Boot 3.3
*   **Storage:** PostgreSQL (Entities), Redis (Counters/Locks)
*   **Messaging:** Apache Kafka (Producer/Consumer), Zookeeper
*   **Testing:** JUnit 5, Mockito, K6 (Load Testing)
*   **DevOps:** Docker, Docker Compose

---

## ⚡ How to Run Locally

### Prerequisites
*   Docker Desktop installed and running.
*   Java 21 SDK.

### 1. Start Infrastructure
Spin up Postgres, Redis, Kafka, and Zookeeper in detached mode.
```bash
docker-compose up -d
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

### 3. Test the API
You can use `curl` or Postman.
```bash
curl -X POST "http://localhost:8080/v1/orders?productId=1&userId=101" \
     -H "Idempotency-Key: unique-uuid-12345" \
     -H "X-User-Id: 101"
```

*   **Response:** `200 OK` (Order Accepted).
*   **Check Logs:** You will see the Producer send the event and the Consumer save it to the DB asynchronously.

---

## 🧪 Testing
The project includes a comprehensive Integration Test suite (`ConcurrencyTest.java`) that simulates race conditions using `ExecutorService`.

```bash
# Run Integration Tests
mvn test
```
**Test Logic:**
1.  Spawns 20 concurrent threads trying to buy 10 items.
2.  Uses `CountDownLatch` to ensure simultaneous execution.
3.  **Asserts:** Exactly 10 `PROCESSED` and 10 `FAILED` orders. Inventory = 0.

---

## 🔮 Future Improvements
*   **Distributed Tracing:** Implement Zipkin/Sleuth to trace requests through Kafka.
*   **Inventory Rollback:** Implement a Saga pattern to rollback Redis inventory if the DB save fails (currently handled via DLQ for manual review).
*   **Kubernetes:** Deploy to Minikube with KEDA for auto-scaling based on Kafka lag.

---
```