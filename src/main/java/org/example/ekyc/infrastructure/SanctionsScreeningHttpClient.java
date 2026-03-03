package org.example.ekyc.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.ekyc.application.SanctionsScreeningClient;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SanctionsScreeningHttpClient implements SanctionsScreeningClient {

    private final ExternalHttpClient httpClient;
    private final String baseUrl;

    @Autowired
    public SanctionsScreeningHttpClient(ExternalHttpClient httpClient) {
        this(httpClient, "http://localhost:8084");
    }

    public SanctionsScreeningHttpClient(ExternalHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public VerificationResult verify(VerificationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("customer_id", request.customer().customerId());
        payload.put("full_name", request.customer().fullName());
        payload.put("date_of_birth", request.customer().dateOfBirth() == null ? null : request.customer().dateOfBirth().toString());
        payload.put("nationality", request.customer().nationality());

        SanctionsServiceResponse response = httpClient.post(
                baseUrl + "/api/v1/check-sanctions",
                payload,
                SanctionsServiceResponse.class
        );

        return new VerificationResult(
                "SANCTIONS",
                response.status(),
                null,
                null,
                response.matchCount(),
                response.matches() == null ? List.of() : response.matches(),
                true
        );
    }

    private record SanctionsServiceResponse(
            String status,
            @JsonProperty("match_count") Integer matchCount,
            List<String> matches
    ) {
    }
}
