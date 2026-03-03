package org.example.ekyc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.ekyc.application.KycOrchestrator;
import org.example.ekyc.domain.Customer;
import org.example.ekyc.domain.KycDecisionResponse;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class KycController {

    private final KycOrchestrator orchestrator;

    public KycController(KycOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/verify-kyc")
    public KycDecisionResponse verifyKyc(@RequestBody VerifyKycApiRequest request) {
        Customer customer = new Customer(
                request.customer().customerId(),
                request.customer().fullName(),
                request.customer().dateOfBirth(),
                request.customer().email(),
                request.customer().phone(),
                request.customer().address()
        );

        VerificationRequest verificationRequest = new VerificationRequest(
                request.requestId(),
                customer,
                request.documentType(),
                request.documentNumber(),
                request.documentExpiryDate(),
                request.documentImageUrl(),
                request.selfieUrl(),
                request.idPhotoUrl(),
                request.proofType(),
                request.proofDate(),
                request.proofUrl()
        );

        return orchestrator.orchestrateWithResults(customer, request.verificationTypes(), verificationRequest);
    }

    public record VerifyKycApiRequest(
            @JsonProperty("request_id") String requestId,
            CustomerApiRequest customer,
            @JsonProperty("verification_types") List<VerificationType> verificationTypes,
            @JsonProperty("document_type") String documentType,
            @JsonProperty("document_number") String documentNumber,
            @JsonProperty("document_expiry_date") LocalDate documentExpiryDate,
            @JsonProperty("document_image_url") String documentImageUrl,
            @JsonProperty("selfie_url") String selfieUrl,
            @JsonProperty("id_photo_url") String idPhotoUrl,
            @JsonProperty("proof_type") String proofType,
            @JsonProperty("proof_date") LocalDate proofDate,
            @JsonProperty("proof_url") String proofUrl
    ) {
    }

    public record CustomerApiRequest(
            @JsonProperty("customer_id") String customerId,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("date_of_birth") LocalDate dateOfBirth,
            String email,
            String phone,
            String address
    ) {
    }
}
