package org.example.ekyc.infrastructure;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.example.ekyc.domain.Customer;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationServiceClientsTest {

    private MockWebServer server;
    private ExternalHttpClient externalHttpClient;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        externalHttpClient = new ExternalHttpClient();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void documentClientShouldCallDocumentEndpointAndReturnVerificationResult() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"PASS\",\"confidence\":95,\"reasons\":[]}"));

        DocumentVerificationHttpClient client = new DocumentVerificationHttpClient(externalHttpClient, baseUrl());
        VerificationResult result = client.verify(sampleRequest());
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

        assertThat(request.getPath()).isEqualTo("/api/v1/verify-document");
        assertThat(result.serviceName()).isEqualTo("ID_DOCUMENT");
        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.confidenceScore()).isEqualTo(95);
        assertThat(result.serviceCallSuccessful()).isTrue();
    }

    @Test
    void biometricClientShouldCallFaceMatchEndpointAndReturnVerificationResult() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"PASS\",\"confidence\":92,\"similarity_score\":92.5}"));

        BiometricVerificationHttpClient client = new BiometricVerificationHttpClient(externalHttpClient, baseUrl());
        VerificationResult result = client.verify(sampleRequest());
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

        assertThat(request.getPath()).isEqualTo("/api/v1/face-match");
        assertThat(result.serviceName()).isEqualTo("FACE_MATCH");
        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.confidenceScore()).isEqualTo(92);
        assertThat(result.similarityScore()).isEqualTo(92.5);
        assertThat(result.serviceCallSuccessful()).isTrue();
    }

    @Test
    void addressClientShouldCallAddressEndpointAndReturnVerificationResult() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"PASS\",\"confidence\":88,\"reasons\":[]}"));

        AddressVerificationHttpClient client = new AddressVerificationHttpClient(externalHttpClient, baseUrl());
        VerificationResult result = client.verify(sampleRequest());
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

        assertThat(request.getPath()).isEqualTo("/api/v1/verify-address");
        assertThat(result.serviceName()).isEqualTo("ADDRESS");
        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.confidenceScore()).isEqualTo(88);
        assertThat(result.serviceCallSuccessful()).isTrue();
    }

    @Test
    void sanctionsClientShouldCallSanctionsEndpointAndReturnVerificationResult() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"CLEAR\",\"match_count\":0,\"matches\":[]}"));

        SanctionsScreeningHttpClient client = new SanctionsScreeningHttpClient(externalHttpClient, baseUrl());
        VerificationResult result = client.verify(sampleRequest());
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

        assertThat(request.getPath()).isEqualTo("/api/v1/check-sanctions");
        assertThat(result.serviceName()).isEqualTo("SANCTIONS");
        assertThat(result.status()).isEqualTo("CLEAR");
        assertThat(result.matchCount()).isEqualTo(0);
        assertThat(result.serviceCallSuccessful()).isTrue();
    }

    private String baseUrl() {
        String value = server.url("/").toString();
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private VerificationRequest sampleRequest() {
        Customer customer = new Customer(
                "CUST-001",
                "Jane Doe",
                LocalDate.of(1990, 5, 15),
                "jane.doe@example.com",
                "+1-555-0123",
                "123 Main St, Springfield, IL 62701"
        );

        return new VerificationRequest(
                "REQ-001",
                customer,
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
}
