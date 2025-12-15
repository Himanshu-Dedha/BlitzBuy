
---

````markdown
# ⚡ BlitzBuy — High-Throughput Flash Sale Engine

> **An event-driven backend system designed to handle massive traffic spikes with strict inventory guarantees, low latency, and high fault tolerance.**

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-green?style=for-the-badge&logo=spring)
![Redis](https://img.shields.io/badge/Redis-Atomic_Operations-red?style=for-the-badge&logo=redis)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-black?style=for-the-badge&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Durable_Storage-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker)

---

## 📖 Overview

**BlitzBuy** addresses the classic **Flash Sale Problem**:

> Selling limited inventory (e.g., 100 items) to tens of thousands of concurrent users without:
1. **Overselling** due to race conditions  
2. **Database collapse** under connection saturation  
3. **High latency** caused by blocking I/O  

Traditional RDBMS approaches (`SELECT ... FOR UPDATE`) provide strong consistency but severely limit throughput.  
BlitzBuy shifts the **critical state** to **Redis** for ultra-fast atomic operations and uses **Apache Kafka** for **peak load leveling**, ensuring database writes occur at a sustainable rate.

---

## 🏗 Architecture

BlitzBuy follows an **event-driven architecture** that separates:

- **Latency-sensitive operations** (user request path)
- **Throughput-sensitive operations** (durable persistence)

```mermaid
graph TD
    User[Client] -->|HTTP POST| API[API Gateway / Controller]

    subgraph "Hot Path (Synchronous, <50ms)"
        API --> RateLimiter[Rate Limit Interceptor]
        RateLimiter --> Idempotency[Idempotency Check]
        Idempotency --> Redis[(Redis)]
        Redis -->|Atomic DECR| Redis
    end

    subgraph "Cold Path (Asynchronous, Durable)"
        Redis --> Producer[Kafka Producer]
        Producer --> Kafka[(Apache Kafka)]
        Kafka --> Consumer[Order Consumer]
        Consumer --> DB[(PostgreSQL)]
        Consumer -->|Failure| DLQ[Dead Letter Queue]
    end

    API <-- 202 Accepted --> User
````

---

## 🚀 Key Engineering Challenges & Solutions

### 1. Concurrency & Lost Update Anomaly

**Problem:**
Under high concurrency, traditional read-modify-write database transactions cause race conditions, resulting in overselling.

**Initial Approach:**
Implemented pessimistic locking (`SELECT ... FOR UPDATE`) in PostgreSQL. While correct, it serialized access and destroyed throughput.

**Final Solution:**
Moved inventory state to **Redis Atomic Counters**.
Using `DECR`, inventory updates occur atomically in-memory, eliminating race conditions while sustaining high throughput.

---

### 2. Peak Load Leveling

**Problem:**
Direct database writes cannot scale to 10k+ requests per second without exhausting connection pools.

**Solution:**
Applied the **Write-Behind Pattern**:

* API acknowledges requests immediately after Redis validation
* Order events are published to Kafka
* Background consumers persist data at a controlled rate

This decoupling prevents database overload while maintaining a responsive user experience.

---

### 3. Distributed Resilience

* **Idempotency:**
  Redis-based `SETNX` idempotency keys prevent duplicate orders caused by retries or double-clicks.
* **Rate Limiting:**
  Custom Redis sliding-window limiter (via Spring `HandlerInterceptor`) caps abusive traffic.
* **Fault Tolerance:**
  Kafka consumers use **exponential backoff** (1s → 2s → 4s). Failed messages are routed to a **Dead Letter Queue (DLQ)**, guaranteeing no data loss.

---

## 📊 Performance Benchmarks

Load testing performed using **K6** on local development hardware (Docker + WSL2).

| Metric                 | Result        | Notes                                                                   |
| ---------------------- | ------------- | ----------------------------------------------------------------------- |
| **Throughput**         | **1,778 TPS** | Sustained for 30s                                                       |
| **Success Rate**       | **100%**      | No dropped requests                                                     |
| **Inventory Accuracy** | **100%**      | Zero overselling                                                        |
| **P95 Latency**        | ~500ms*       | *Inflated due to local Docker networking; projected <50ms in cloud VPC* |

---

## 🛠 Tech Stack

* **Language:** Java 21, Spring Boot 3.3
* **Caching / Sync:** Redis (atomic counters, idempotency, rate limiting)
* **Messaging:** Apache Kafka, Zookeeper
* **Persistence:** PostgreSQL
* **Testing:** JUnit 5, Mockito, K6
* **Infrastructure:** Docker, Docker Compose

---

## ⚡ Running Locally

### Prerequisites

* Docker Desktop
* Java 21

### Start Infrastructure

```bash
docker-compose up -d
```

### Run Application

```bash
./mvnw spring-boot:run
```

### Test API

```bash
curl -X POST "http://localhost:8080/v1/orders?productId=1&userId=101" \
  -H "Idempotency-Key: unique-uuid-12345" \
  -H "X-User-Id: 101"
```

Expected Response:

* `202 Accepted` — Order successfully queued

---

## 🧪 Testing Strategy

Includes an integration test suite (`ConcurrencyTest.java`) simulating race conditions using `ExecutorService`.

**Test Flow:**

1. Spawn 20 concurrent threads attempting to purchase 10 items
2. Synchronize execution with `CountDownLatch`
3. Assert:

    * 10 successful orders
    * 10 rejected orders
    * Final inventory = 0

```bash
mvn test
```

---

## 🔮 Future Enhancements

* Distributed tracing with Zipkin / Sleuth
* Saga-based inventory rollback for DB failures
* Kubernetes deployment with KEDA auto-scaling based on Kafka lag

---

