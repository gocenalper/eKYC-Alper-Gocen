package org.example.ekyc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ekyc.application.DecisionEngine;
import org.example.ekyc.application.KycOrchestrator;
import org.example.ekyc.domain.Customer;
import org.example.ekyc.domain.KycDecision;
import org.example.ekyc.domain.KycDecisionResponse;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;
import org.example.ekyc.domain.VerificationType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KycControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldReturnApprovedDecisionFromVerifyKycEndpoint() throws Exception {
        KycController controller = new KycController(new StubKycOrchestrator(KycDecision.APPROVED));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        KycController.VerifyKycApiRequest request = sampleApiRequest();

                mockMvc.perform(post("/api/v1/verify-kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.verification_results").isArray())
                .andExpect(jsonPath("$.verification_results[0].verification_type").value("ID_DOCUMENT"))
                .andExpect(jsonPath("$.verification_results[0].confidence").value(95))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private KycController.VerifyKycApiRequest sampleApiRequest() {
        return new KycController.VerifyKycApiRequest(
                "REQ-001",
                new KycController.CustomerApiRequest(
                        "CUST-001",
                        "Jane Doe",
                        LocalDate.of(1990, 5, 15),
                        "jane.doe@example.com",
                        "+1-555-0123",
                        "123 Main St, Springfield, IL 62701"
                ),
                List.of(VerificationType.ID_DOCUMENT, VerificationType.FACE_MATCH, VerificationType.ADDRESS, VerificationType.SANCTIONS),
                "PASSPORT",
                "P12345678",
                LocalDate.now().plusDays(365),
                "https://doc.example/passport.jpg",
                "https://selfie.example/me.jpg",
                "https://doc.example/id-photo.jpg",
                "UTILITY_BILL",
                LocalDate.now().minusDays(30),
                "https://proof.example/bill.pdf"
        );
    }

    private static class StubKycOrchestrator extends KycOrchestrator {
        private final KycDecision decision;

        StubKycOrchestrator(KycDecision decision) {
            super(
                    request -> null,
                    request -> null,
                    request -> null,
                    request -> null,
                    new DecisionEngine()
            );
            this.decision = decision;
        }

        @Override
        public KycDecisionResponse orchestrateWithResults(Customer customer, List<VerificationType> verificationTypes, VerificationRequest request) {
            return new KycDecisionResponse(
                    decision.name(),
                    List.of(new VerificationResult("ID_DOCUMENT", "PASS", 95, null, null, List.of(), true))
            );
        }
    }
}
