package org.example.ekyc.domain;

import java.util.List;

public record DocumentVerificationResult(
        String status,
        int confidence,
        List<String> reasons
) {
}
