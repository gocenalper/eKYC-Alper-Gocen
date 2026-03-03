package org.example.ekyc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record VerificationResult(
        @JsonProperty("verification_type") String serviceName,
        String status,
        @JsonProperty("confidence") Integer confidenceScore,
        @JsonIgnore Double similarityScore,
        @JsonIgnore Integer matchCount,
        List<String> reasons,
        @JsonIgnore boolean serviceCallSuccessful,
        String timestamp
) {
    public VerificationResult(
            String serviceName,
            String status,
            Integer confidenceScore,
            Double similarityScore,
            Integer matchCount,
            List<String> reasons,
            boolean serviceCallSuccessful
    ) {
        this(
                serviceName,
                status,
                confidenceScore,
                similarityScore,
                matchCount,
                reasons,
                serviceCallSuccessful,
                Instant.now().toString()
        );
    }
}
