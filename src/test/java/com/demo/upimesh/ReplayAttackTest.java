package com.demo.upimesh;

import com.demo.upimesh.model.MeshPacket;
import com.demo.upimesh.service.BridgeIngestionService;
import com.demo.upimesh.service.DemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Replay attack defense test.
 * Submit the exact same packet twice (with a small delay).
 * First submission should SETTLE.
 * Second submission should be rejected as DUPLICATE.
 */
@SpringBootTest
public class ReplayAttackTest {

    @Autowired private DemoService demoService;
    @Autowired private BridgeIngestionService ingestion;

    @Test
    public void samePacketSubmittedTwice_SecondIsDuplicate() throws Exception {
        // Use DemoService to create a properly signed packet
        MeshPacket packet = demoService.createPacket(
                "alice@demo", "bob@demo", new BigDecimal("50.00"), "1234", 5);

        // First submission — should settle
        BridgeIngestionService.IngestResult first = ingestion.ingest(packet, "bridge-A", 1);
        assertEquals("SETTLED", first.outcome(),
                "First submission should settle successfully");

        // Second submission (same packet) — should be duplicate
        BridgeIngestionService.IngestResult second = ingestion.ingest(packet, "bridge-A", 1);
        assertEquals("DUPLICATE_DROPPED", second.outcome(),
                "Second submission of same packet should be rejected as duplicate");
    }
}