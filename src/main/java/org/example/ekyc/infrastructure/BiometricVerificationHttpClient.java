package org.example.ekyc.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.ekyc.application.BiometricVerificationClient;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BiometricVerificationHttpClient implements BiometricVerificationClient {

    private final ExternalHttpClient httpClient;
    private final String baseUrl;

    @Autowired
    public BiometricVerificationHttpClient(ExternalHttpClient httpClient) {
        this(httpClient, "http://localhost:8082");
    }

    public BiometricVerificationHttpClient(ExternalHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public VerificationResult verify(VerificationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("customer_id", request.customer().customerId());
        payload.put("selfie_url", request.selfieUrl());
        payload.put("id_photo_url", request.idPhotoUrl());

        BiometricServiceResponse response = httpClient.post(
                baseUrl + "/api/v1/face-match",
                payload,
                BiometricServiceResponse.class
        );

        return new VerificationResult(
                "FACE_MATCH",
                response.status(),
                response.confidence(),
                response.similarityScore(),
                null,
                List.of(),
                true
        );
    }

    private record BiometricServiceResponse(
            String status,
            Integer confidence,
            @JsonProperty("similarity_score") Double similarityScore
    ) {
    }
}
