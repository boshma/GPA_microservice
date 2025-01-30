package com.microservice.user_service.IntegrationTests;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpTestUtil {
    private final HttpClient webClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String authToken;

    public HttpTestUtil(HttpClient webClient, ObjectMapper objectMapper, String baseUrl, String apiKey) {
        this(webClient, objectMapper, baseUrl, apiKey, null);
    }

    public HttpTestUtil(HttpClient webClient, ObjectMapper objectMapper, String baseUrl, String apiKey, String authToken) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.authToken = authToken;
    }

    public HttpResponse<String> sendRequest(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        if (apiKey != null) {
            requestBuilder.header("X-API-Key", apiKey);
        }
        
        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        switch (method.toUpperCase()) {
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
                break;
            case "PUT":
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body));
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            default:
                requestBuilder.GET();
        }

        return webClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public JsonNode parseResponse(HttpResponse<String> response) throws IOException {
        return objectMapper.readTree(response.body());
    }
}