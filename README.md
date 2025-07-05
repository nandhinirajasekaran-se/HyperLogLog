# 🚀 Real-Time Unique User Tracking using HyperLogLog

This project demonstrates how to estimate the number of **unique users per site** using:
- Apache Kafka for real-time event ingestion
- Spring Boot for Kafka consumers and Redis HLL updates
- Redis for efficient memory-based HyperLogLog storage
- Docker Compose for container orchestration

---

## 📊 Why HyperLogLog?

HyperLogLog is a probabilistic algorithm that estimates cardinality (number of distinct elements) using **very low memory** (≈1.5 KB per counter), perfect for large-scale stream processing.

---

## 🧩 Architecture

