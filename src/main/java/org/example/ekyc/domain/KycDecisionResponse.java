package org.example.ekyc.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record KycDecisionResponse(
        String decision,
        @JsonProperty("verification_results") List<VerificationResult> verificationResults,
        String timestamp
) {
    public KycDecisionResponse(String decision, List<VerificationResult> verificationResults) {
        this(decision, verificationResults, Instant.now().toString());
    }
}
