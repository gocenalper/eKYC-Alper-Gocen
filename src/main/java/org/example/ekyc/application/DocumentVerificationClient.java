package org.example.ekyc.application;

import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;

public interface DocumentVerificationClient {
    VerificationResult verify(VerificationRequest request);
}
