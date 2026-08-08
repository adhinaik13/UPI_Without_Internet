package com.demo.upimesh;

import com.demo.upimesh.crypto.HybridCryptoService;
import com.demo.upimesh.crypto.ServerKeyHolder;
import com.demo.upimesh.model.MeshPacket;
import com.demo.upimesh.model.PaymentInstruction;
import com.demo.upimesh.service.BridgeIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReplayAttackTest {

    @Autowired private HybridCryptoService crypto;
    @Autowired private BridgeIngestionService ingestion;
    @Autowired private ServerKeyHolder serverKey;

    @Test
    public void samePacketSubmittedTwice_SecondIsDuplicate() throws Exception {
        long signedAt = System.currentTimeMillis();
        long expiresAt = signedAt + 86400000;

        PaymentInstruction instruction = new PaymentInstruction(
                "alice@demo", "bob@demo", new BigDecimal("50.00"),
                "pin-hash", "nonce-123", Long.valueOf(signedAt), Long.valueOf(expiresAt)
        );

        String ciphertext = crypto.encrypt(instruction, serverKey.getPublicKey());
        MeshPacket packet = new MeshPacket();
        packet.setPacketId("packet-1");
        packet.setTtl(5);
        packet.setCreatedAt(System.currentTimeMillis());
        packet.setCiphertext(ciphertext);

        BridgeIngestionService.IngestResult first = ingestion.ingest(packet, "bridge-A", 1);
        assertEquals("SETTLED", first.outcome(),
                "First submission should settle successfully");

        BridgeIngestionService.IngestResult second = ingestion.ingest(packet, "bridge-A", 1);
        assertEquals("DUPLICATE_DROPPED", second.outcome(),
                "Second submission of same packet should be rejected as duplicate");
    }
}