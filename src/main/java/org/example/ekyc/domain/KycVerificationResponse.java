package org.example.ekyc.domain;

import java.util.List;

public record KycVerificationResponse(
        String status,
        List<String> failureReasons
) {
}
