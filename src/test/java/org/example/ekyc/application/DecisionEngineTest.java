package org.example.ekyc.application;

import org.example.ekyc.domain.KycDecision;
import org.example.ekyc.domain.VerificationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionEngineTest {

    private final DecisionEngine decisionEngine = new DecisionEngine();

    @Test
    void test_all_verifications_pass() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.APPROVED);
    }

    @Test
    void test_sanctions_hit() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("HIT", 1, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.REJECTED);
    }

    @Test
    void test_low_confidence_scores() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("FAIL", 70, 90.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.MANUAL_REVIEW);
    }

    @Test
    void test_expired_document() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("FAIL", 95, List.of("DOCUMENT_EXPIRED"), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.REJECTED);
    }

    @Test
    void shouldReturnApprovedWhenAllChecksPass() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.APPROVED);
    }

    @Test
    void shouldReturnRejectedWhenSanctionsHasHit() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("HIT", 1, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.REJECTED);
    }

    @Test
    void shouldReturnRejectedWhenSanctionsServiceFails() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, false)
        ));

        assertThat(decision).isEqualTo(KycDecision.REJECTED);
    }

    @Test
    void shouldReturnRejectedWhenDocumentIsExpired() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("FAIL", 95, List.of("DOCUMENT_EXPIRED"), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.REJECTED);
    }

    @Test
    void shouldReturnManualReviewWhenDocumentConfidenceIsLow() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("FAIL", 70, List.of("LOW_CONFIDENCE"), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.MANUAL_REVIEW);
    }

    @Test
    void shouldReturnManualReviewWhenFaceMatchScoresAreLow() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("FAIL", 82, 80.0, true),
                address("PASS", 88, List.of(), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.MANUAL_REVIEW);
    }

    @Test
    void shouldReturnManualReviewWhenAddressProofIsOld() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("PASS", 91, 92.0, true),
                address("FAIL", 88, List.of("PROOF_OLDER_THAN_90_DAYS"), true),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.MANUAL_REVIEW);
    }

    @Test
    void shouldReturnManualReviewWhenNonCriticalServiceFails() {
        KycDecision decision = decisionEngine.decide(List.of(
                document("PASS", 95, List.of(), true),
                biometric("PASS", 91, 92.0, true),
                address("PASS", 88, List.of(), false),
                sanctions("CLEAR", 0, true)
        ));

        assertThat(decision).isEqualTo(KycDecision.MANUAL_REVIEW);
    }

    private VerificationResult document(String status, Integer confidence, List<String> reasons, boolean success) {
        return new VerificationResult("ID_DOCUMENT", status, confidence, null, null, reasons, success);
    }

    private VerificationResult biometric(String status, Integer confidence, Double similarity, boolean success) {
        return new VerificationResult("FACE_MATCH", status, confidence, similarity, null, List.of(), success);
    }

    private VerificationResult address(String status, Integer confidence, List<String> reasons, boolean success) {
        return new VerificationResult("ADDRESS", status, confidence, null, null, reasons, success);
    }

    private VerificationResult sanctions(String status, Integer matchCount, boolean success) {
        return new VerificationResult("SANCTIONS", status, null, null, matchCount, List.of(), success);
    }
}
