package org.example.ekyc.domain;

import java.util.List;

public record SanctionsScreeningResult(
        String status,
        int matchCount,
        List<String> matches
) {
}
