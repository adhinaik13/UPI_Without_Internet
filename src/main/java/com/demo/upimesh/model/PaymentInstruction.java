package com.demo.upimesh.model;

import java.math.BigDecimal;

/**
 * Plaintext payment instruction that gets encrypted into a MeshPacket.
 * Includes a digital signature so the settlement gateway can verify
 * that the payment was actually authorized by the claimed sender.
 */
public class PaymentInstruction {

    private String senderVpa;
    private String receiverVpa;
    private BigDecimal amount;
    private String pinHash;
    private String nonce;        // UUID, unique per payment intent
    private Long signedAt;       // epoch millis, when sender signed
    private Long expiresAt;      // epoch millis, when payment expires (24h window)

    // Digital signature fields
    private String senderPublicKeyFingerprint; // SHA-256 fingerprint of sender's pubkey
    private String signature;                  // RSA-PSS signature over canonical data

    public PaymentInstruction() {}

    public PaymentInstruction(String senderVpa, String receiverVpa, BigDecimal amount,
                              String pinHash, String nonce, Long signedAt, Long expiresAt) {
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.amount = amount;
        this.pinHash = pinHash;
        this.nonce = nonce;
        this.signedAt = signedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Canonical bytes for signing/verification.
     * Excludes the signature field itself to avoid circular dependency.
     */
    public byte[] canonicalBytes() {
        String canonical = senderVpa + "|" + receiverVpa + "|" + amount.toPlainString()
                + "|" + nonce + "|" + signedAt + "|" + expiresAt;
        return canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public String getSenderVpa() { return senderVpa; }
    public void setSenderVpa(String senderVpa) { this.senderVpa = senderVpa; }

    public String getReceiverVpa() { return receiverVpa; }
    public void setReceiverVpa(String receiverVpa) { this.receiverVpa = receiverVpa; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public Long getSignedAt() { return signedAt; }
    public void setSignedAt(Long signedAt) { this.signedAt = signedAt; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    public String getSenderPublicKeyFingerprint() { return senderPublicKeyFingerprint; }
    public void setSenderPublicKeyFingerprint(String senderPublicKeyFingerprint) { this.senderPublicKeyFingerprint = senderPublicKeyFingerprint; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}