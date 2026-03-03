package org.example.ekyc.application;

import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;

public interface SanctionsScreeningClient {
    VerificationResult verify(VerificationRequest request);
}
