package org.example.ekyc.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Component
public class ExternalHttpClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeout;

    public ExternalHttpClient() {
        this(HttpClient.newBuilder().connectTimeout(DEFAULT_TIMEOUT).build(), new ObjectMapper(), DEFAULT_TIMEOUT);
    }

    public ExternalHttpClient(HttpClient httpClient, ObjectMapper objectMapper, Duration timeout) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.timeout = timeout;
    }

    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) {
                throw new ExternalServiceException("External service returned server error: " + response.statusCode());
            }
            if (response.statusCode() >= 400) {
                throw new ExternalServiceException("External service returned client error: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), responseType);
        } catch (HttpTimeoutException exception) {
            throw new ExternalServiceException("External service request timed out", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("External service request interrupted", exception);
        } catch (IOException exception) {
            throw new ExternalServiceException("External service request failed", exception);
        }
    }
}
