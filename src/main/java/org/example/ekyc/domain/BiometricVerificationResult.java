package org.example.ekyc.domain;

public record BiometricVerificationResult(
        String status,
        int confidence,
        double similarityScore
) {
}
