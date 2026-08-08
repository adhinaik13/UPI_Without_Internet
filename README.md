[![Java CI](https://github.com/adhinaik13/UPI_Without_Internet/actions/workflows/ci.yml/badge.svg)](https://github.com/adhinaik13/UPI_Without_Internet/actions)

# 🔐 UPI Offline Mesh — Cryptographic Offline Payments

[![Java 17](https://img.shields.io/badge/Java-17-blue)](https://adoptium.net/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

&gt; **Offline peer-to-peer payments with cryptographic settlement.**
&gt; Two phones with zero connectivity can exchange value via QR codes. When any device gains internet, the payment settles atomically with authenticated encryption and idempotent processing.

&gt; ⚠️ **Current Status:** This repository implements the **backend settlement gateway** and **QR-code payment simulation**. The mesh networking layer (Bluetooth LE, WiFi Direct) is architected and simulated server-side; native Android transport is the next integration phase.

---

## 🏗️ Architecture

┌─────────────────┐      ┌─────────────────┐      ┌─────────────────────────┐
│  Sender Phone   │      │   Mesh Network  │      │   Spring Boot Gateway   │
│   (Offline)     │      │                 │      │                         │
│                 │      │                 │      │  ├─ SHA-256 hash          │
│ ├─ Compose tx   │─────►│ ├─ Store & fwd  │─────►│  ├─ Atomic claim          │
│ ├─ AES-GCM enc  │      │ ├─ Gossip       │      │  ├─ Decrypt + verify      │
│ ├─ RSA-OAEP     │      │ └─ Bridge nodes │      │  └─ Debit/Credit (atomic) │
│ └─ Display QR   │      │                 │      │                         │
└─────────────────┘      └─────────────────┘      └─────────────────────────┘

---

## 🚀 Live Demo

**Try it now:** `http://localhost:8080/qr-demo`

Generate an encrypted offline payment QR code in seconds. Scan it with any phone camera.

### 🎥 Demo Video
[Watch on YouTube](https://youtu.be/_jcLwtK2IOU) — Full offline payment flow: QR generation → mesh propagation → settlement

---

## ✨ What Makes This Special

| Feature | What It Does | Impact |
|---------|-------------|--------|
| **🔐 Hybrid Encryption** | RSA-OAEP + AES-256-GCM with authenticated encryption | Confidentiality + integrity for offline payloads |
| **📱 QR Code Offline Payments** | Generate encrypted payment QR codes without internet | Works anywhere, any phone |
| **🌐 Mesh Gossip Protocol** | Bluetooth-style store-and-forward across devices | No infrastructure needed |
| **⚡ Atomic Idempotency** | SHA-256 hash + `putIfAbsent` = exactly one settlement | Prevents double-spending under concurrent load |
| **🛡️ Hardware Security Planned** | Android StrongBox / TEE integration roadmap | Keys never leave secure element |
| **📊 Real-time Dashboard** | Thymeleaf + Web UI for monitoring | Transaction visibility |
| **📚 Auto API Docs** | Swagger/OpenAPI at `/swagger-ui.html` | Interactive API documentation |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot 3.3, Java 17 |
| **Security** | RSA-2048 OAEP, AES-256-GCM, SHA-256 |
| **Database** | H2 (dev) / PostgreSQL (prod) |
| **Cache** | ConcurrentHashMap (dev) / Redis (prod) |
| **QR Codes** | ZXing (Google) |
| **Build** | Maven, Docker Compose |
| **Docs** | OpenAPI 3.0 / Swagger UI |

---

## 🚀 Quick Start

### Prerequisites
- JDK 17+
- Maven (wrapper included)

### Run Locally
```bash
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
