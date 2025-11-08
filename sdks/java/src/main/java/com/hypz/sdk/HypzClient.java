package com.hypz.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Hypz SDK Client for Java/Android.
 */
public class HypzClient {

    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private final long connectTimeout;
    private final long readTimeout;
    private final long writeTimeout;
    private final String userAgent;

    // Managers
    private final BucketManager bucketManager;
    private final FileManager fileManager;
    private final ApiKeyManager apiKeyManager;
    private final UsageManager usageManager;

    private HypzClient(Builder builder) {
        this.apiKey = builder.apiKey;
        this.baseUrl = builder.baseUrl.endsWith("/")
            ? builder.baseUrl.substring(0, builder.baseUrl.length() - 1)
            : builder.baseUrl;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.userAgent = builder.userAgent != null ? builder.userAgent : "Hypz-Java-SDK/1.0.0";

        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Math.max(1, builder.connectTimeout)))
            .build();

        this.gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

        this.bucketManager = new BucketManager(this);
        this.fileManager = new FileManager(this);
        this.apiKeyManager = new ApiKeyManager(this);
        this.usageManager = new UsageManager(this);
    }

    /**
     * Get bucket operations manager.
     */
    public BucketManager buckets() {
        return bucketManager;
    }

    /**
     * Get file operations manager.
     */
    public FileManager files() {
        return fileManager;
    }

    /**
     * Get API key operations manager.
     */
    public ApiKeyManager apiKeys() {
        return apiKeyManager;
    }

    /**
     * Get usage statistics manager.
     */
    public UsageManager usage() {
        return usageManager;
    }

    String getBaseUrl() {
        return baseUrl;
    }

    Gson getGson() {
        return gson;
    }

    HttpRequest.Builder baseRequest(String endpoint) {
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + endpoint))
            .timeout(Duration.ofSeconds(Math.max(1, readTimeout)))
            .header("X-API-Key", apiKey)
            .header("User-Agent", userAgent)
            .header("Accept", "application/json");
    }

    HypzResponse get(String endpoint) throws IOException {
        return sendRequest("GET", endpoint, null, null);
    }

    HypzResponse post(String endpoint, Object data) throws IOException {
        String json = data != null ? gson.toJson(data) : "{}";
        return sendRequest(
            "POST",
            endpoint,
            HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8),
            Collections.singletonMap("Content-Type", "application/json")
        );
    }

    HypzResponse put(String endpoint, Object data) throws IOException {
        String json = data != null ? gson.toJson(data) : "{}";
        return sendRequest(
            "PUT",
            endpoint,
            HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8),
            Collections.singletonMap("Content-Type", "application/json")
        );
    }

    HypzResponse delete(String endpoint) throws IOException {
        return sendRequest("DELETE", endpoint, null, null);
    }

    HypzResponse sendRequest(
        String method,
        String endpoint,
        HttpRequest.BodyPublisher bodyPublisher,
        Map<String, String> headers
    ) throws IOException {
        HttpRequest.Builder builder = baseRequest(endpoint);

        if (headers != null) {
            headers.forEach(builder::header);
        }

        HttpRequest request;
        if (("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && bodyPublisher == null) {
            if ("GET".equalsIgnoreCase(method)) {
                request = builder.GET().build();
            } else {
                request = builder.DELETE().build();
            }
        } else {
            HttpRequest.BodyPublisher publisher = bodyPublisher != null
                ? bodyPublisher
                : HttpRequest.BodyPublishers.noBody();
            request = builder.method(method.toUpperCase(), publisher).build();
        }

        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return toHypzResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    byte[] sendForBytes(HttpRequest.Builder builder) throws IOException {
        try {
            HttpResponse<byte[]> response = httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofByteArray()
            );
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw new IOException("Request failed with status " + response.statusCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    private HypzResponse toHypzResponse(HttpResponse<String> response) {
        String responseBody = response.body() != null ? response.body() : "{}";
        JsonObject json;
        try {
            json = gson.fromJson(responseBody, JsonObject.class);
        } catch (Exception ex) {
            json = new JsonObject();
        }

        boolean success = json.has("success")
            ? json.get("success").getAsBoolean()
            : response.statusCode() >= 200 && response.statusCode() < 300;
        String message = json.has("message") ? json.get("message").getAsString() : null;
        JsonElement data = json.has("data") ? json.get("data") : null;

        return new HypzResponse(response.statusCode(), success, message, data);
    }

    /**
     * Builder for HypzClient.
     */
    public static class Builder {
        private String apiKey;
        private String baseUrl = "http://localhost:5000/api/v1";
        private long connectTimeout = 30;
        private long readTimeout = 30;
        private long writeTimeout = 30;
        private String userAgent;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder connectTimeout(long seconds) {
            this.connectTimeout = seconds;
            return this;
        }

        public Builder readTimeout(long seconds) {
            this.readTimeout = seconds;
            return this;
        }

        public Builder writeTimeout(long seconds) {
            this.writeTimeout = seconds;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public HypzClient build() {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("API key is required");
            }
            return new HypzClient(this);
        }
    }

    /**
     * Response wrapper used by the SDK.
     */
    public static class HypzResponse {
        private final int statusCode;
        private final boolean success;
        private final String message;
        private final JsonElement data;

        HypzResponse(int statusCode, boolean success, String message, JsonElement data) {
            this.statusCode = statusCode;
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public JsonElement getData() {
            return data;
        }

        public <T> T getDataAs(Class<T> clazz) {
            if (data == null) {
                return null;
            }
            return new Gson().fromJson(data, clazz);
        }
    }
}
