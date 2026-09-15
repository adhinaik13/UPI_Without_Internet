# ADR 001: Hybrid Encryption with RSA-OAEP + AES-256-GCM

## Status

Accepted

## Context

The payment payload (a JSON `PaymentInstruction`) contains sensitive data: sender VPA, receiver VPA, amount, and PIN hash. This payload must be encrypted before being encoded into a QR code or transmitted over an untrusted mesh network.

Two constraints drove the design:

1. **Payload size:** A payment instruction is approximately 300-500 bytes of JSON. RSA-2048 can only encrypt approximately 245 bytes directly.

2. **Performance:** RSA encryption/decryption is significantly slower than AES. Encrypting the full payload with RSA would be impractical on a low-power mobile device.

## Decision

Use **hybrid encryption**:

- **AES-256-GCM** for the payload (fast, authenticated)
- **RSA-OAEP** for the AES key (asymmetric protection)

## Why RSA-OAEP and not PKCS#1 v1.5?

PKCS#1 v1.5 padding is vulnerable to Bleichenbacher's oracle attack. OAEP (Optimal Asymmetric Encryption Padding) is the modern padding scheme for RSA encryption and is specified in RFC 8017.

## Why AES-GCM and not AES-CBC + HMAC?

AES-CBC requires a separate HMAC for integrity. GCM provides authenticated encryption in a single operation and is widely used in modern secure communication protocols.

## Why not ECIES (Elliptic Curve Integrated Encryption Scheme)?

ECIES is elegant and provides smaller ciphertexts. However:

- RSA keypairs are easier to generate and manage in Java's standard library
- RSA public keys can be distributed as simple base64 strings
- The performance difference is acceptable for our payload size
- RSA-OAEP is widely understood by reviewers

**Future:** If we add forward secrecy, we may switch to ECDHE + AES-GCM.

## Why SHA-256 for the transaction fingerprint?

SHA-256 is:

- Standard and widely supported
- Fast enough for our use case
- Collision-resistant for 64-hex-character digests

We do not need SHA-3 or BLAKE3 because the transaction fingerprint is used for duplicate detection rather than as a password or key derivation function.

## Consequences

- **Positive:** Well-understood, widely supported, and performant
- **Negative:** No forward secrecy; a compromised server key could expose encrypted traffic protected by that key
- **Mitigation:** Plan to add ephemeral ECDHE key exchange in Phase 2