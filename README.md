# 🔐 UPI Offline Mesh — Cryptographic Offline Payments

[![Java 17](https://img.shields.io/badge/Java-17-blue)](https://adoptium.net/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

&gt; **Offline peer-to-peer payments with cryptographic settlement.**  
&gt; Two phones in a basement with zero connectivity can exchange value via QR codes. When any device gains internet, the payment settles atomically with bank-grade security.

---

## 🚀 Live Demo

**Try it now:** `http://localhost:8080/qr-demo`

Generate an encrypted offline payment QR code in seconds. Scan it with any phone camera.

---

## ✨ What Makes This Special

| Feature | What It Does | Impact |
|---------|-------------|--------|
| **🔐 Hybrid Encryption** | RSA-OAEP + AES-256-GCM with authenticated encryption | Military-grade security, tamper-proof |
| **📱 QR Code Offline Payments** | Generate encrypted payment QR codes without internet | Works anywhere, any phone |
| **🌐 Mesh Gossip Protocol** | Bluetooth-style store-and-forward across devices | No infrastructure needed |
| **⚡ Atomic Idempotency** | SHA-256 hash + `putIfAbsent` = exactly one settlement | Prevents double-spending |
| **🛡️ Hardware Security Ready** | Android StrongBox / TEE integration planned | Keys never leave secure element |
| **📊 Real-time Dashboard** | Thymeleaf + Web UI for monitoring | Production observability |
| **📚 Auto API Docs** | Swagger/OpenAPI at `/swagger-ui.html` | Professional documentation |

---

## 🏗️ Architecture
