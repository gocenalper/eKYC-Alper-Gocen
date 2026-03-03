package org.example.ekyc.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelsTest {

    @Test
    void shouldCreateCustomerWithIdentityFields() {
        Customer customer = new Customer(
                "CUST-001",
                "Jane Doe",
                LocalDate.of(1990, 5, 15),
                "jane.doe@example.com",
                "+1-555-0123",
                "123 Main St, Springfield, IL 62701"
        );

        assertThat(customer.customerId()).isEqualTo("CUST-001");
        assertThat(customer.fullName()).isEqualTo("Jane Doe");
        assertThat(customer.dateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(customer.email()).isEqualTo("jane.doe@example.com");
        assertThat(customer.phone()).isEqualTo("+1-555-0123");
    }

    @Test
    void shouldCreateVerificationRequestWithCustomerAndEvidenceFields() {
        Customer customer = new Customer(
                "CUST-001",
                "Jane Doe",
                LocalDate.of(1990, 5, 15),
                "jane.doe@example.com",
                "+1-555-0123",
                "123 Main St, Springfield, IL 62701"
        );

        VerificationRequest request = new VerificationRequest(
                "REQ-001",
                customer,
                "PASSPORT",
                "P12345678",
                LocalDate.of(2027, 12, 31),
                "https://doc.example/passport.jpg",
                "https://selfie.example/me.jpg",
                "https://doc.example/id-photo.jpg",
                "UTILITY_BILL",
                LocalDate.of(2025, 12, 15),
                "https://proof.example/bill.pdf"
        );

        assertThat(request.requestId()).isEqualTo("REQ-001");
        assertThat(request.customer()).isEqualTo(customer);
        assertThat(request.documentType()).isEqualTo("PASSPORT");
        assertThat(request.proofType()).isEqualTo("UTILITY_BILL");
    }

    @Test
    void shouldCreateVerificationResultWithServiceOutcomeFields() {
        VerificationResult result = new VerificationResult(
                "document",
                "PASS",
                95,
                null,
                null,
                List.of(),
                true
        );

        assertThat(result.serviceName()).isEqualTo("document");
        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.confidenceScore()).isEqualTo(95);
        assertThat(result.serviceCallSuccessful()).isTrue();
    }

    @Test
    void shouldExposeExpectedKycDecisionValues() {
        assertThat(KycDecision.values())
                .containsExactly(KycDecision.APPROVED, KycDecision.REJECTED, KycDecision.MANUAL_REVIEW);
    }
}
