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

    public HttpTestUtil(HttpClient webClient, ObjectMapper objectMapper, String baseUrl, String apiKey) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public HttpResponse<String> sendRequest(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-API-Key", apiKey);

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