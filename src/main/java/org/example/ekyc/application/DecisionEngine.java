package org.example.ekyc.application;

import org.example.ekyc.domain.KycDecision;
import org.example.ekyc.domain.VerificationResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DecisionEngine {

    public KycDecision decide(List<VerificationResult> results) {
        if (results == null || results.isEmpty()) {
            return KycDecision.REJECTED;
        }

        Map<String, VerificationResult> byService = results.stream()
                .collect(Collectors.toMap(
                        result -> normalize(result.serviceName()),
                        Function.identity(),
                        (first, ignored) -> first
                ));

        VerificationResult sanctions = byService.get("SANCTIONS");
        if (sanctions == null || !sanctions.serviceCallSuccessful()) {
            return KycDecision.REJECTED;
        }

        if ("HIT".equalsIgnoreCase(sanctions.status()) || valueOrZero(sanctions.matchCount()) > 0) {
            return KycDecision.REJECTED;
        }

        VerificationResult document = byService.get("ID_DOCUMENT");
        if (document == null || !document.serviceCallSuccessful()) {
            return KycDecision.MANUAL_REVIEW;
        }

        if (hasReason(document, "DOCUMENT_EXPIRED")) {
            return KycDecision.REJECTED;
        }

        if (valueOrZero(document.confidenceScore()) <= 85 || !"PASS".equalsIgnoreCase(document.status())) {
            return KycDecision.MANUAL_REVIEW;
        }

        VerificationResult biometric = byService.get("FACE_MATCH");
        if (biometric == null || !biometric.serviceCallSuccessful()) {
            return KycDecision.MANUAL_REVIEW;
        }

        if (valueOrZero(biometric.confidenceScore()) <= 85
                || valueOrZero(biometric.similarityScore()) <= 85.0
                || !"PASS".equalsIgnoreCase(biometric.status())) {
            return KycDecision.MANUAL_REVIEW;
        }

        VerificationResult address = byService.get("ADDRESS");
        if (address == null || !address.serviceCallSuccessful()) {
            return KycDecision.MANUAL_REVIEW;
        }

        if (hasReason(address, "PROOF_OLDER_THAN_90_DAYS")
                || valueOrZero(address.confidenceScore()) <= 80
                || !"PASS".equalsIgnoreCase(address.status())) {
            return KycDecision.MANUAL_REVIEW;
        }

        return KycDecision.APPROVED;
    }

    private boolean hasReason(VerificationResult result, String reason) {
        return result.reasons() != null && result.reasons().stream()
                .filter(item -> item != null)
                .map(this::normalize)
                .anyMatch(normalized -> normalized.equals(normalize(reason)));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }
}
