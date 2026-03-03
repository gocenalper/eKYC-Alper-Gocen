package org.example.ekyc.application;

import org.example.ekyc.domain.Customer;
import org.example.ekyc.domain.KycDecision;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;
import org.example.ekyc.domain.VerificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KycOrchestratorTest {

    @Test
    void shouldReturnApprovedForHappyPath() {
        KycOrchestrator orchestrator = new KycOrchestrator(
                request -> result("ID_DOCUMENT", "PASS", 95, null, null, List.of(), true),
                request -> result("FACE_MATCH", "PASS", 92, 92.5, null, List.of(), true),
                request -> result("ADDRESS", "PASS", 88, null, null, List.of(), true),
                request -> result("SANCTIONS", "CLEAR", null, null, 0, List.of(), true),
                new DecisionEngine()
        );

        KycDecision decision = orchestrator.orchestrate(sampleCustomer(), List.of(
                VerificationType.ID_DOCUMENT,
                VerificationType.FACE_MATCH,
                VerificationType.ADDRESS,
                VerificationType.SANCTIONS
        ), sampleRequest());

        assertThat(decision).isEqualTo(KycDecision.APPROVED);
    }

    @Test
    void shouldReturnRejectedWhenSanctionsHits() {
        KycOrchestrator orchestrator = new KycOrchestrator(
                request -> result("ID_DOCUMENT", "PASS", 95, null, null, List.of(), true),
                request -> result("FACE_MATCH", "PASS", 92, 92.5, null, List.of(), true),
                request -> result("ADDRESS", "PASS", 88, null, null, List.of(), true),
                request -> result("SANCTIONS", "HIT", null, null, 1, List.of("OFAC"), true),
                new DecisionEngine()
        );

        KycDecision decision = orchestrator.orchestrate(sampleCustomer(), List.of(
                VerificationType.ID_DOCUMENT,
                VerificationType.FACE_MATCH,
                VerificationType.ADDRESS,
                VerificationType.SANCTIONS
        ), sampleRequest());

        assertThat(decision).isEqualTo(KycDecision.REJECTED);
    }

    @Test
    void shouldReturnManualReviewWhenNonCriticalServiceFails() {
        KycOrchestrator orchestrator = new KycOrchestrator(
                request -> result("ID_DOCUMENT", "PASS", 95, null, null, List.of(), true),
                request -> {
                    throw new RuntimeException("biometric timeout");
                },
                request -> result("ADDRESS", "PASS", 88, null, null, List.of(), true),
                request -> result("SANCTIONS", "CLEAR", null, null, 0, List.of(), true),
                new DecisionEngine()
        );

        KycDecision decision = orchestrator.orchestrate(sampleCustomer(), List.of(
                VerificationType.ID_DOCUMENT,
                VerificationType.FACE_MATCH,
                VerificationType.ADDRESS,
                VerificationType.SANCTIONS
        ), sampleRequest());

        assertThat(decision).isEqualTo(KycDecision.MANUAL_REVIEW);
    }

    private VerificationResult result(
            String serviceName,
            String status,
            Integer confidenceScore,
            Double similarityScore,
            Integer matchCount,
            List<String> reasons,
            boolean serviceCallSuccessful
    ) {
        return new VerificationResult(serviceName, status, confidenceScore, similarityScore, matchCount, reasons, serviceCallSuccessful);
    }

    private Customer sampleCustomer() {
        return new Customer(
                "CUST-001",
                "Jane Doe",
                LocalDate.of(1990, 5, 15),
                "jane.doe@example.com",
                "+1-555-0123",
                "123 Main St, Springfield, IL 62701"
        );
    }

    private VerificationRequest sampleRequest() {
        return new VerificationRequest(
                "REQ-001",
                sampleCustomer(),
                "PASSPORT",
                "P12345678",
                LocalDate.now().plusDays(365),
                "https://doc.example/passport.jpg",
                "https://selfie.example/me.jpg",
                "https://doc.example/id-photo.jpg",
                "UTILITY_BILL",
                LocalDate.now().minusDays(30),
                "https://proof.example/bill.pdf"
        );
    }
}
