package org.example.ekyc.infrastructure;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalHttpClientTest {

    private MockWebServer server;
    private ExternalHttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new ExternalHttpClient();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldPostJsonAndParseResponseBody() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"PASS\",\"confidence\":95}"));

        SampleResponse response = client.post(
                server.url("/api/v1/verify-document").toString(),
                Map.of("customer_id", "CUST-001"),
                SampleResponse.class
        );

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);

        assertThat(response.status()).isEqualTo("PASS");
        assertThat(response.confidence()).isEqualTo(95);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/api/v1/verify-document");
        assertThat(request.getBody().readUtf8()).contains("\"customer_id\":\"CUST-001\"");
    }

    @Test
    void shouldThrowExceptionWhenServiceReturns500() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\":\"internal\"}"));

        assertThatThrownBy(() -> client.post(
                server.url("/api/v1/check-sanctions").toString(),
                Map.of("customer_id", "CUST-001"),
                SampleResponse.class
        )).isInstanceOf(ExternalServiceException.class)
          .hasMessageContaining("500");
    }

    @Test
    void shouldThrowExceptionWhenRequestTimesOutAtDefault5Seconds() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setHeadersDelay(6, TimeUnit.SECONDS)
                .setBody("{\"status\":\"PASS\",\"confidence\":95}"));

        assertThatThrownBy(() -> client.post(
                server.url("/api/v1/face-match").toString(),
                Map.of("customer_id", "CUST-001"),
                SampleResponse.class
        )).isInstanceOf(ExternalServiceException.class)
          .hasMessageContaining("timed out");
    }

    private record SampleResponse(String status, int confidence) {
    }
}
