package com.hypz.sdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * File operations manager powered by the java.net.http client.
 */
public class FileManager {

    private final HypzClient client;

    FileManager(HypzClient client) {
        this.client = client;
    }

    public HypzClient.HypzResponse upload(String bucketId, File file, Map<String, String> metadata, String[] tags) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        Map<String, String> fields = new LinkedHashMap<>();
        if (metadata != null && !metadata.isEmpty()) {
            fields.put("metadata", client.getGson().toJson(metadata));
        }
        if (tags != null && tags.length > 0) {
            fields.put("tags", String.join(",", tags));
        }

        MultipartPayload payload = buildMultipartPayload(file.getName(), fileBytes, fields);
        return client.sendRequest(
            "POST",
            "/files/" + bucketId + "/upload",
            HttpRequest.BodyPublishers.ofByteArray(payload.body),
            Collections.singletonMap("Content-Type", "multipart/form-data; boundary=" + payload.boundary)
        );
    }

    public HypzClient.HypzResponse upload(String bucketId, File file) throws IOException {
        return upload(bucketId, file, null, null);
    }

    public HypzClient.HypzResponse uploadBytes(String bucketId, String fileName, byte[] bytes) throws IOException {
        MultipartPayload payload = buildMultipartPayload(fileName, bytes, Collections.emptyMap());
        return client.sendRequest(
            "POST",
            "/files/" + bucketId + "/upload",
            HttpRequest.BodyPublishers.ofByteArray(payload.body),
            Collections.singletonMap("Content-Type", "multipart/form-data; boundary=" + payload.boundary)
        );
    }

    public HypzClient.HypzResponse list(String bucketId, int page, int limit) throws IOException {
        return client.get("/files/" + bucketId + "/files?page=" + page + "&limit=" + limit);
    }

    public HypzClient.HypzResponse list(String bucketId) throws IOException {
        return list(bucketId, 1, 20);
    }

    public HypzClient.HypzResponse get(String fileId) throws IOException {
        return client.get("/files/file/" + fileId);
    }

    public void download(String fileId, File outputFile) throws IOException {
        HttpRequest.Builder builder = client.baseRequest("/files/file/" + fileId + "/download");
        builder.header("Accept", "*/*");
        byte[] data = client.sendForBytes(builder);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(data);
        }
    }

    public byte[] downloadBytes(String fileId) throws IOException {
        HttpRequest.Builder builder = client.baseRequest("/files/file/" + fileId + "/download");
        builder.header("Accept", "*/*");
        return client.sendForBytes(builder);
    }

    public HypzClient.HypzResponse update(String fileId, Map<String, Object> updates) throws IOException {
        return client.put("/files/file/" + fileId, updates);
    }

    public HypzClient.HypzResponse delete(String fileId) throws IOException {
        return client.delete("/files/file/" + fileId);
    }

    public String getPublicUrl(String fileId) {
        return client.getBaseUrl() + "/files/public/" + fileId + "/download";
    }

    public HypzClient.HypzResponse bulkDownload(String[] fileIds) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fileIds", Arrays.asList(fileIds));
        return client.post("/files/bulk/download", payload);
    }

    public HypzClient.HypzResponse initiatePresignedUpload(String bucketId, PresignedUploadOptions options) throws IOException {
        if (bucketId == null || bucketId.isEmpty()) {
            throw new IllegalArgumentException("bucketId is required");
        }
        if (options == null || options.fileName == null || options.fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName is required");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("filename", options.fileName);
        payload.put("mimeType", options.mimeType != null ? options.mimeType : "application/octet-stream");

        if (options.fileSize != null) {
            payload.put("size", options.fileSize);
        }
        if (options.tags != null && !options.tags.isEmpty()) {
            payload.put("tags", options.tags);
        }
        if (options.metadata != null && !options.metadata.isEmpty()) {
            payload.put("metadata", options.metadata);
        }

        return client.post("/files/" + bucketId + "/files/presigned", payload);
    }

    public HypzClient.HypzResponse completePresignedUpload(String fileId, PresignedUploadCompletion completion) throws IOException {
        if (fileId == null || fileId.isEmpty()) {
            throw new IllegalArgumentException("fileId is required");
        }
        if (completion == null || completion.b2FileId == null || completion.b2FileId.isEmpty()) {
            throw new IllegalArgumentException("b2FileId is required to complete presigned uploads");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("b2FileId", completion.b2FileId);

        if (completion.finalSize != null) {
            payload.put("finalSize", completion.finalSize);
        }
        if (completion.sha1 != null && !completion.sha1.isEmpty()) {
            payload.put("sha1", completion.sha1);
        }
        if (completion.partCount != null) {
            payload.put("partCount", completion.partCount);
        }
        if (completion.tags != null && !completion.tags.isEmpty()) {
            payload.put("tags", completion.tags);
        }
        if (completion.metadata != null && !completion.metadata.isEmpty()) {
            payload.put("metadata", completion.metadata);
        }

        return client.post("/files/file/" + fileId + "/complete", payload);
    }

    public static class PresignedUploadOptions {
        private final String fileName;
        private Long fileSize;
        private String mimeType;
        private List<String> tags;
        private Map<String, Object> metadata;

        public PresignedUploadOptions(String fileName) {
            this.fileName = fileName;
        }

        public PresignedUploadOptions fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public PresignedUploadOptions mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public PresignedUploadOptions tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public PresignedUploadOptions metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
    }

    public static class PresignedUploadCompletion {
        private String b2FileId;
        private Long finalSize;
        private String sha1;
        private Integer partCount;
        private List<String> tags;
        private Map<String, Object> metadata;

        public PresignedUploadCompletion b2FileId(String b2FileId) {
            this.b2FileId = b2FileId;
            return this;
        }

        public PresignedUploadCompletion finalSize(Long finalSize) {
            this.finalSize = finalSize;
            return this;
        }

        public PresignedUploadCompletion sha1(String sha1) {
            this.sha1 = sha1;
            return this;
        }

        public PresignedUploadCompletion partCount(Integer partCount) {
            this.partCount = partCount;
            return this;
        }

        public PresignedUploadCompletion tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public PresignedUploadCompletion metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
    }

    private MultipartPayload buildMultipartPayload(String filename, byte[] fileBytes, Map<String, String> fields) throws IOException {
        String boundary = "----HypzBoundary" + UUID.randomUUID();
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
            .append(filename)
            .append("\"\r\n");
        writer.append("Content-Type: application/octet-stream\r\n\r\n");
        writer.flush();

        output.write(fileBytes);
        writer.append("\r\n");

        if (fields != null) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"")
                    .append(entry.getKey())
                    .append("\"\r\n\r\n");
                writer.append(entry.getValue()).append("\r\n");
            }
        }

        writer.append("--").append(boundary).append("--\r\n");
        writer.flush();

        return new MultipartPayload(output.toByteArray(), boundary);
    }

    private static class MultipartPayload {
        final byte[] body;
        final String boundary;

        MultipartPayload(byte[] body, String boundary) {
            this.body = body;
            this.boundary = boundary;
        }
    }
}
