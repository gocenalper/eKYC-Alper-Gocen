package org.example.ekyc.domain;

import java.util.List;

public record AddressVerificationResult(
        String status,
        int confidence,
        List<String> reasons
) {
}
