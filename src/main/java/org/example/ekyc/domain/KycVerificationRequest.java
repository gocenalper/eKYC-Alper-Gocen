package org.example.ekyc.domain;

public record KycVerificationRequest(
        String customerId,
        String documentType,
        String documentNumber,
        String expiryDate,
        String documentImageUrl,
        String selfieUrl,
        String idPhotoUrl,
        String address,
        String proofType,
        String proofDate,
        String proofUrl,
        String fullName,
        String dateOfBirth,
        String nationality
) {
}
