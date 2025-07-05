# 🚀 Real-Time Unique User Tracking using HyperLogLog

This project demonstrates how to estimate the number of **unique users per site** using:

- Apache Kafka for real-time event ingestion  
- Spring Boot for Kafka consumers and Redis HLL updates  
- Redis for efficient memory-based HyperLogLog storage  
- Docker Compose for container orchestration

---

## 📊 Why HyperLogLog?

HyperLogLog is a probabilistic algorithm that estimates cardinality (number of distinct elements) using **very low memory** (~1.5 KB per counter), perfect for large-scale stream processing.

---

## 🧩 Architecture

```
[ Producers ] --> [ Kafka Topic: events ] --> [ Spring Boot Consumer ]
                                           --> [ Redis PFADD hll:siteId userId ]
```

---

## ⚙️ Technologies

- Java 21 + Spring Boot 3.2  
- Apache Kafka  
- Redis 7  
- Docker + Docker Compose  

---

## 💡 Sample Kafka Event

```json
{
  "user_id": "u1234",
  "site_id": "siteA",
  "timestamp": "2025-07-04T15:00:00Z"
}
```

---

## 📦 Redis Keys

Each `site_id` has a corresponding Redis HLL key:  
```
hll:siteA
```

Use Redis CLI to check:

```bash
redis-cli PFCOUNT hll:siteA
```

---

## 🔄 Running the Project

```bash
git clone https://github.com/your-username/hyperloglog-unique-counter.git
cd hyperloglog-unique-counter
docker-compose up --build
```

Then test Kafka:

```bash
docker exec -it kafka bash
kafka-console-producer --broker-list kafka:9092 --topic events
```

Paste:
```json
{"user_id":"u1", "site_id":"siteA", "timestamp":"now"}
```

---

## ✅ REST Endpoint (Optional)

You can add a REST API to query HLL counts:

```http
GET /stats/siteA
```

Returns:
```json
{"site_id": "siteA", "estimated_users": 2391}
```

---

## 🧠 Learnings

- How to use Redis HyperLogLog in production  
- How to integrate Kafka Streams with Spring Boot  
- Dockerized microservices for real-time analytics

---

## 📄 License

MIT

---

## 🙌 Acknowledgements

Built as a personal deep dive into Redis probabilistic data structures and Kafka-based streaming.
