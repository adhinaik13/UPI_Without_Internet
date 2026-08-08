\# Threat Model — UPI Offline Mesh



\## Scope



This document analyzes the security properties of the backend settlement gateway and the cryptographic protocol used for offline peer-to-peer payment exchange.



\*\*In scope:\*\*

\- Payment payload confidentiality and integrity

\- Sender authentication

\- Replay attack prevention

\- Duplicate settlement prevention

\- Tamper detection



\*\*Out of scope:\*\*

\- Bluetooth LE / WiFi Direct transport security (not yet implemented)

\- Hardware-backed key storage (planned future work)

\- Formal protocol certification

\- Production deployment hardening



\---



\## Asset Inventory



| Asset | Value | Protection Mechanism |

|-------|-------|----------------------|

| Payment payload (amount, sender, receiver) | High | AES-256-GCM encryption |

| Device private keys | Critical | In-memory only (simulated; real: StrongBox/TEE) |

| Server private key | Critical | ServerKeyHolder (file-based in dev) |

| Transaction ledger | High | DB unique constraint + optimistic locking |

| Account balances | High | Atomic debit/credit with `@Version` |



\---



\## Threat Actors



| Actor | Capability | Motivation |

|-------|-----------|------------|

| Passive eavesdropper | Can read QR codes / mesh traffic | Learn payment details |

| Active man-in-the-middle | Can modify packets in transit | Forge payments, steal funds |

| Malicious bridge node | Can replay, drop, or duplicate packets | Double-spend, denial of service |

| Compromised sender device | Has access to private key | Sign unauthorized payments |

| Rogue gateway operator | Controls settlement server | Settle invalid transactions |



\---



\## Attack Scenarios \& Defenses



\### 1. Eavesdropping (Confidentiality)



\*\*Threat:\*\* Attacker reads QR code or intercepts mesh packet.



\*\*Defense:\*\* Hybrid encryption (RSA-OAEP + AES-256-GCM). Without the server's private key, the attacker cannot decrypt the AES key, and without the AES key, cannot decrypt the payload.



\*\*Residual risk:\*\* None within threat model. Server private key compromise is out of scope.



\---



\### 2. Tampering (Integrity)



\*\*Threat:\*\* Attacker modifies ciphertext in transit.



\*\*Defense:\*\* AES-GCM authenticated encryption. Any single-bit modification causes decryption to fail with `AEADBadTagException`.



\*\*Residual risk:\*\* Attacker could craft a new valid ciphertext if they had the AES key, but the AES key is encrypted with RSA-OAEP and only the server can decrypt it.



\---



\### 3. Replay Attack



\*\*Threat:\*\* Attacker captures a valid payment packet and submits it again later.



\*\*Defense:\*\* Three layers:

1\. \*\*`nonce`\*\* — UUID unique per payment intent; same nonce = same packet

2\. \*\*`signedAt` + `expiresAt`\*\* — Time-bounded validity (default 24h)

3\. \*\*Idempotency cache + DB unique constraint\*\* — Prevents duplicate settlement even if nonce is forgotten



\*\*Residual risk:\*\* If clock skew exceeds 5 minutes, future-dated packets are rejected. If server clock is manipulated, time checks fail.



\---



\### 4. Duplicate Settlement (Double-Spend)



\*\*Threat:\*\* Same packet reaches gateway through multiple bridge nodes simultaneously.



\*\*Defense:\*\* Three layers:

1\. \*\*`ConcurrentHashMap` idempotency cache\*\* — Fast in-memory duplicate detection

2\. \*\*DB unique index on `packetHash`\*\* — Persistent duplicate protection

3\. \*\*`@Version` optimistic locking on Account\*\* — Prevents lost-update race conditions during debit/credit



\*\*Tested:\*\* `IdempotencyConcurrencyTest` simulates 3 threads delivering the same packet concurrently. Exactly 1 settles, 2 are rejected.



\*\*Residual risk:\*\* If cache and DB constraint both fail simultaneously (extremely unlikely), optimistic locking on balance prevents incorrect state.



\---



\### 5. Sender Impersonation



\*\*Threat:\*\* Attacker forges a payment claiming to be Alice.



\*\*Defense:\*\* RSA-PSS digital signatures. Each device has a unique 2048-bit RSA keypair. The private key never leaves the device. The gateway verifies the signature against the registered public key before settlement.



\*\*Residual risk:\*\* If device private key is stolen (e.g., rooted phone), attacker can sign valid payments. Mitigation: hardware-backed keys (StrongBox/TEE) — planned future work.



\---



\### 6. Forged Signature



\*\*Threat:\*\* Attacker creates a fake signature without the private key.



\*\*Defense:\*\* RSA-PSS is provably secure under the RSA assumption. Forging a signature without the private key is computationally infeasible for 2048-bit keys.



\*\*Residual risk:\*\* Quantum computing (Shor's algorithm) — out of scope.



\---



\### 7. Sybil Attack (Malicious Bridge Nodes)



\*\*Threat:\*\* Attacker creates many fake bridge nodes to flood the network.



\*\*Defense:\*\* Not yet implemented. The mesh simulation does not authenticate bridge nodes.



\*\*Planned mitigation:\*\* Bridge node registration with proof-of-work or stake-based reputation.



\---



\### 8. Key Compromise



\*\*Threat:\*\* Server private key is leaked.



\*\*Impact:\*\* All past and future payments can be decrypted. Signatures remain valid (device keys are separate).



\*\*Mitigation:\*\* Key rotation, HSM storage — planned future work.



\---



\## Security Property Summary



| Property | Mechanism | Status |

|----------|-----------|--------|

| Confidentiality | AES-256-GCM | ✅ |

| Integrity | AES-GCM auth tag | ✅ |

| Sender authentication | RSA-PSS signatures | ✅ |

| Replay resistance | nonce + timestamp + idempotency | ✅ |

| Duplicate settlement prevention | Cache + DB constraint + optimistic locking | ✅ |

| Non-repudiation | RSA-PSS signatures | ✅ |

| Forward secrecy | None (static RSA keys) | ⚠️ Planned: ECDHE |

| Hardware security | None (software keys) | ⚠️ Planned: StrongBox |



\---



\## What This Is NOT



This is a \*\*research/educational prototype\*\*, not a production UPI implementation. It does not:

\- Integrate with NPCI or real bank settlement rails

\- Use hardware security modules

\- Have formal security certification

\- Handle Byzantine fault tolerance

\- Provide forward secrecy



These are intentional scope boundaries, not oversights.

