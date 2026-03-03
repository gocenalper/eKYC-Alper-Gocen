package org.example.ekyc.infrastructure;

import org.example.ekyc.application.AddressVerificationClient;
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
public class AddressVerificationHttpClient implements AddressVerificationClient {

    private final ExternalHttpClient httpClient;
    private final String baseUrl;

    @Autowired
    public AddressVerificationHttpClient(ExternalHttpClient httpClient) {
        this(httpClient, "http://localhost:8083");
    }

    public AddressVerificationHttpClient(ExternalHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public VerificationResult verify(VerificationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("customer_id", request.customer().customerId());
        payload.put("address", request.customer().address());
        payload.put("proof_type", request.proofType());
        payload.put("proof_date", request.proofDate() == null ? null : request.proofDate().toString());
        payload.put("proof_url", request.proofUrl());

        AddressServiceResponse response = httpClient.post(
                baseUrl + "/api/v1/verify-address",
                payload,
                AddressServiceResponse.class
        );

        List<String> reasons = new ArrayList<>(response.reasons() == null ? List.of() : response.reasons());
        String status = response.status();
        LocalDate threshold = LocalDate.now().minusDays(90);
        if (request.proofDate() != null && request.proofDate().isBefore(threshold)) {
            reasons.add("PROOF_OLDER_THAN_90_DAYS");
            status = "FAIL";
        }

        return new VerificationResult(
                "ADDRESS",
                status,
                response.confidence(),
                null,
                null,
                reasons,
                true
        );
    }

    private record AddressServiceResponse(String status, Integer confidence, List<String> reasons) {
    }
}
