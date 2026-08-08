\# ADR 001: Hybrid Encryption with RSA-OAEP + AES-256-GCM



\## Status



Accepted



\## Context



The payment payload (a JSON `PaymentInstruction`) contains sensitive data: sender VPA, receiver VPA, amount, and PIN hash. This payload must be encrypted before being encoded into a QR code or transmitted over an untrusted mesh network.



Two constraints drove the design:

1\. \*\*Payload size:\*\* A payment instruction is \~300–500 bytes of JSON. RSA-2048 can only encrypt \~245 bytes directly.

2\. \*\*Performance:\*\* RSA encryption/decryption is \~1000× slower than AES. Encrypting the full payload with RSA would be impractical on a low-power mobile device.



\## Decision



Use \*\*hybrid encryption\*\*:

\- \*\*AES-256-GCM\*\* for the payload (fast, authenticated)

\- \*\*RSA-OAEP\*\* for the AES key (asymmetric protection)



\## Why RSA-OAEP and not PKCS#1 v1.5?



PKCS#1 v1.5 padding is vulnerable to Bleichenbacher's oracle attack. OAEP (Optimal Asymmetric Encryption Padding) is provably secure in the random oracle model and is the modern standard (RFC 8017).



\## Why AES-GCM and not AES-CBC + HMAC?



AES-CBC requires a separate HMAC for integrity. GCM provides authenticated encryption in a single pass, is faster, and is the standard for TLS 1.3, IPsec, and SSH.



\## Why not ECIES (Elliptic Curve Integrated Encryption Scheme)?



ECIES is elegant and provides smaller ciphertexts. However:

\- RSA keypairs are easier to generate and manage in Java's standard library

\- RSA public keys can be distributed as simple base64 strings

\- The performance difference is negligible for our payload size (\~500 bytes)

\- RSA-OAEP is more widely understood by interviewers and reviewers



\*\*Future:\*\* If we add forward secrecy, we may switch to ECDHE + AES-GCM.



\## Why SHA-256 for the transaction fingerprint?



SHA-256 is:

\- Standard (NIST-approved)

\- Fast enough for our use case

\- Collision-resistant for 64-hex-character strings



We do not need SHA-3 or BLAKE3 because we are not defending against length-extension attacks (the hash input is a base64 ciphertext, not a structured message).



\## Consequences



\- \*\*Positive:\*\* Well-understood, widely supported, performant

\- \*\*Negative:\*\* No forward secrecy (compromised server key decrypts all past traffic)

\- \*\*Mitigation:\*\* Plan to add ephemeral ECDHE key exchange in Phase 2

