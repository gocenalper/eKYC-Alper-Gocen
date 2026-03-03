package org.example.ekyc.infrastructure;

import org.example.ekyc.application.DocumentVerificationClient;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DocumentVerificationHttpClient implements DocumentVerificationClient {

    private final ExternalHttpClient httpClient;
    private final String baseUrl;

    @Autowired
    public DocumentVerificationHttpClient(ExternalHttpClient httpClient) {
        this(httpClient, "http://localhost:8081");
    }

    public DocumentVerificationHttpClient(ExternalHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public VerificationResult verify(VerificationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("customer_id", request.customer().customerId());
        payload.put("document_type", request.documentType());
        payload.put("document_number", request.documentNumber());
        payload.put("expiry_date", request.documentExpiryDate() == null ? null : request.documentExpiryDate().toString());
        payload.put("document_image_url", request.documentImageUrl());

        DocumentServiceResponse response = httpClient.post(
                baseUrl + "/api/v1/verify-document",
                payload,
                DocumentServiceResponse.class
        );

        List<String> reasons = new ArrayList<>(response.reasons() == null ? List.of() : response.reasons());
        String status = response.status();

        if (request.documentExpiryDate() != null && request.documentExpiryDate().isBefore(LocalDate.now())) {
            reasons.add("DOCUMENT_EXPIRED");
            status = "FAIL";
        }

        return new VerificationResult(
                "ID_DOCUMENT",
                status,
                response.confidence(),
                null,
                null,
                reasons,
                true
        );
    }

    private record DocumentServiceResponse(String status, Integer confidence, List<String> reasons) {
    }
}
