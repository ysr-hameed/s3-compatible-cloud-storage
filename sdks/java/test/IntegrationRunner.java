package com.hypz.sdk.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypz.sdk.FileManager;
import com.hypz.sdk.HypzClient;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IntegrationRunner {

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("HYPZ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("HYPZ_API_KEY environment variable is required");
            System.exit(1);
        }

        String baseUrl = System.getenv("HYPZ_API_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:5000/api/v1";
        }

        HypzClient client = new HypzClient.Builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .build();

        String bucketName = "java-sdk-test-" + Instant.now().toEpochMilli();
        HypzClient.HypzResponse bucketResponse = client.buckets().create(bucketName, "private", "Java SDK integration test");
        JsonObject bucketData = requireData(bucketResponse);
        String bucketId = bucketData.get("id").getAsString();

        String primaryFileId = null;
        String presignedFileId = null;
        Path tempFile = null;

        try {
            // Upload regular file
            tempFile = Files.createTempFile("hypz-java-sdk-", ".txt");
            Files.writeString(tempFile, "Hello from the Hypz Java SDK!");
            File file = tempFile.toFile();

            HypzClient.HypzResponse uploadResponse = client.files().upload(
                bucketId,
                file,
                Map.of("source", "java-sdk", "purpose", "integration-test"),
                new String[] { "integration", "java" }
            );
            JsonObject uploadData = requireData(uploadResponse);
            primaryFileId = uploadData.get("id").getAsString();

            // Presigned upload flow
            byte[] presignedBytes = "Direct upload via presigned URL".getBytes(StandardCharsets.UTF_8);
            FileManager.PresignedUploadOptions options = new FileManager.PresignedUploadOptions("java-presigned-" + UUID.randomUUID() + ".txt")
                .fileSize((long) presignedBytes.length)
                .mimeType("text/plain")
                .tags(List.of("integration", "presigned"))
                .metadata(Map.of("source", "java-sdk"));

            HypzClient.HypzResponse presignedResponse = client.files().initiatePresignedUpload(bucketId, options);
            JsonObject presignedData = requireData(presignedResponse);
            String uploadUrl = presignedData.get("uploadUrl").getAsString();
            String uploadAuthToken = presignedData.get("uploadAuthToken").getAsString();
            String presignedName = presignedData.get("fileName").getAsString();
            String temporaryFileId = presignedData.get("fileId").getAsString();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest uploadRequest = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", uploadAuthToken)
                .header("X-Bz-File-Name", presignedName)
                .header("Content-Type", "text/plain")
                .header("X-Bz-Content-Sha1", "do_not_verify")
                .POST(HttpRequest.BodyPublishers.ofByteArray(presignedBytes))
                .build();

            HttpResponse<Void> uploadResult = httpClient.send(uploadRequest, HttpResponse.BodyHandlers.discarding());
            if (uploadResult.statusCode() / 100 != 2) {
                throw new IOException("Presigned upload failed with status " + uploadResult.statusCode());
            }

            FileManager.PresignedUploadCompletion completion = new FileManager.PresignedUploadCompletion()
                .finalSize((long) presignedBytes.length);

            HypzClient.HypzResponse completionResponse = client.files().completePresignedUpload(temporaryFileId, completion);
            JsonObject completionData = requireData(completionResponse);
            presignedFileId = completionData.get("id").getAsString();

            System.out.println("Java SDK integration tests completed successfully.");
        } finally {
            if (primaryFileId != null) {
                client.files().delete(primaryFileId);
            }
            if (presignedFileId != null) {
                client.files().delete(presignedFileId);
            }
            client.buckets().delete(bucketId);
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    private static JsonObject requireData(HypzClient.HypzResponse response) {
        if (!response.isSuccess()) {
            throw new IllegalStateException("Request failed: " + response.getMessage());
        }
        JsonElement data = response.getData();
        if (data == null || !data.isJsonObject()) {
            throw new IllegalStateException("Response missing data payload");
        }
        return data.getAsJsonObject();
    }
}
