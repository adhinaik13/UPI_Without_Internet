package com.demo.upimesh.service;

import com.demo.upimesh.crypto.DigitalSignatureService;
import com.demo.upimesh.crypto.HybridCryptoService;
import com.demo.upimesh.crypto.ServerKeyHolder;
import com.demo.upimesh.model.Account;
import com.demo.upimesh.model.AccountRepository;
import com.demo.upimesh.model.MeshPacket;
import com.demo.upimesh.model.PaymentInstruction;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper service that:
 *   - seeds demo accounts on startup
 *   - registers device keypairs for demo accounts
 *   - simulates "sender phone creates an encrypted packet" flow
 */
@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    @Autowired private AccountRepository accounts;
    @Autowired private HybridCryptoService crypto;
    @Autowired private ServerKeyHolder serverKey;
    @Autowired private DigitalSignatureService sigService;
    @Autowired private DeviceIdentityService identity;

    // Simulated device key storage (in a real Android app, this would be in StrongBox/TEE)
    private final Map<String, KeyPair> deviceKeys = new ConcurrentHashMap<>();

    @PostConstruct
    public void seedAccounts() {
        if (accounts.count() == 0) {
            accounts.save(new Account("alice@demo", "Alice",   new BigDecimal("5000.00")));
            accounts.save(new Account("bob@demo",   "Bob",     new BigDecimal("1000.00")));
            accounts.save(new Account("carol@demo", "Carol",   new BigDecimal("2500.00")));
            accounts.save(new Account("dave@demo",  "Dave",    new BigDecimal("500.00")));
            log.info("Seeded 4 demo accounts");
        }

        // Register device keys for demo accounts
        for (String vpa : new String[]{"alice@demo", "bob@demo", "carol@demo", "dave@demo"}) {
            try {
                if (!identity.isRegistered(vpa)) {
                    KeyPair kp = identity.registerDevice(vpa);
                    deviceKeys.put(vpa, kp);
                    log.info("Registered device key for {}", vpa);
                }
            } catch (Exception e) {
                log.warn("Failed to register device key for {}: {}", vpa, e.getMessage());
            }
        }
    }

    /**
     * Simulates the sender's phone:
     *   1. Build a PaymentInstruction with a fresh nonce + signedAt timestamp.
     *   2. Sign with the device's private key.
     *   3. Encrypt with the server's public key (hybrid RSA+AES).
     *   4. Wrap in a MeshPacket with TTL.
     */
    public MeshPacket createPacket(String senderVpa, String receiverVpa,
                                   BigDecimal amount, String pin, int ttl) throws Exception {
        long signedAt = System.currentTimeMillis();
        long expiresAt = signedAt + 24 * 60 * 60 * 1000; // 24 hours from now
        String nonce = UUID.randomUUID().toString();
        String pinHash = sha256Hex(pin);

        PaymentInstruction instruction = new PaymentInstruction(
                senderVpa, receiverVpa, amount, pinHash, nonce,
                Long.valueOf(signedAt), Long.valueOf(expiresAt)
        );

        // Sign with device private key
        KeyPair deviceKey = deviceKeys.get(senderVpa);
        if (deviceKey == null) {
            throw new IllegalStateException("No device key registered for " + senderVpa);
        }
        String fingerprint = sigService.fingerprint(deviceKey.getPublic());
        instruction.setSenderPublicKeyFingerprint(fingerprint);

        byte[] canonical = instruction.canonicalBytes();
        String signature = sigService.sign(canonical, deviceKey.getPrivate());
        instruction.setSignature(signature);

        String ciphertext = crypto.encrypt(instruction, serverKey.getPublicKey());

        MeshPacket packet = new MeshPacket();
        packet.setPacketId(UUID.randomUUID().toString());
        packet.setTtl(ttl);
        packet.setCreatedAt(Instant.now().toEpochMilli());
        packet.setCiphertext(ciphertext);
        return packet;
    }

    private String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
