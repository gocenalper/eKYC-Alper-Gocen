package org.example.ekyc.domain;

import java.time.LocalDate;

public record VerificationRequest(
        String requestId,
        Customer customer,
        String documentType,
        String documentNumber,
        LocalDate documentExpiryDate,
        String documentImageUrl,
        String selfieUrl,
        String idPhotoUrl,
        String proofType,
        LocalDate proofDate,
        String proofUrl
) {
}
