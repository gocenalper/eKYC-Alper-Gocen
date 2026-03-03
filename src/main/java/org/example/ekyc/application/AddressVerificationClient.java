package org.example.ekyc.application;

import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;

public interface AddressVerificationClient {
    VerificationResult verify(VerificationRequest request);
}
