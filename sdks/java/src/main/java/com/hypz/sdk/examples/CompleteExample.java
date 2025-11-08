package com.hypz.sdk.examples;

import com.hypz.sdk.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Complete example demonstrating all Hypz SDK features
 */
public class CompleteExample {
    
    public static void main(String[] args) {
        // Initialize the client
        HypzClient client = new HypzClient.Builder()
            .apiKey("sk_live_your_api_key_here") // Replace with your API key
            .baseUrl("http://localhost:5000/api/v1")
            .connectTimeout(30)
            .readTimeout(30)
            .writeTimeout(30)
            .build();
        
        try {
            // ============================================
            // 1. BUCKET OPERATIONS
            // ============================================
            System.out.println("\n=== BUCKET OPERATIONS ===\n");
            
            // Create a public bucket
            System.out.println("Creating public bucket...");
            HypzClient.HypzResponse createBucket = client.buckets()
                .createPublic("test-bucket-java");
            System.out.println("✓ " + createBucket.getMessage());
            
            String bucketId = createBucket.getData()
                .getAsJsonObject()
                .get("id")
                .getAsString();
            System.out.println("  Bucket ID: " + bucketId);
            
            // List all buckets
            System.out.println("\nListing buckets...");
            HypzClient.HypzResponse buckets = client.buckets().list();
            JsonArray bucketArray = buckets.getData()
                .getAsJsonObject()
                .getAsJsonArray("buckets");
            System.out.println("✓ Found " + bucketArray.size() + " bucket(s)");
            
            // Get bucket details
            System.out.println("\nGetting bucket details...");
            HypzClient.HypzResponse bucket = client.buckets().get(bucketId);
            JsonObject bucketData = bucket.getData().getAsJsonObject();
            System.out.println("✓ Bucket: " + bucketData.get("name").getAsString());
            System.out.println("  Visibility: " + bucketData.get("visibility").getAsString());
            
            // Update bucket
            System.out.println("\nUpdating bucket name...");
            HypzClient.HypzResponse updateBucket = client.buckets()
                .updateName(bucketId, "updated-java-bucket");
            System.out.println("✓ " + updateBucket.getMessage());
            
            // ============================================
            // 2. FILE OPERATIONS
            // ============================================
            System.out.println("\n=== FILE OPERATIONS ===\n");
            
            // Upload a file (using bytes for demo)
            System.out.println("Uploading file...");
            String content = "Hello from Hypz Java SDK! " + System.currentTimeMillis();
            byte[] fileContent = content.getBytes();
            
            HypzClient.HypzResponse upload = client.files()
                .uploadBytes(bucketId, "test-file.txt", fileContent);
            System.out.println("✓ " + upload.getMessage());
            
            String fileId = upload.getData()
                .getAsJsonObject()
                .get("id")
                .getAsString();
            System.out.println("  File ID: " + fileId);
            
            // Upload with metadata and tags
            System.out.println("\nUploading file with metadata...");
            Map<String, String> metadata = new HashMap<>();
            metadata.put("author", "Java SDK Test");
            metadata.put("environment", "test");
            metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            String[] tags = {"test", "java", "sdk"};
            
            // Create a temp file for upload
            File tempFile = File.createTempFile("hypz-test", ".txt");
            java.nio.file.Files.write(tempFile.toPath(), 
                "File with metadata and tags".getBytes());
            
            HypzClient.HypzResponse uploadWithMeta = client.files()
                .upload(bucketId, tempFile, metadata, tags);
            System.out.println("✓ " + uploadWithMeta.getMessage());
            tempFile.delete();
            
            // Wait for files to be indexed
            Thread.sleep(1000);
            
            // List files
            System.out.println("\nListing files...");
            HypzClient.HypzResponse files = client.files().list(bucketId);
            JsonArray fileArray = files.getData()
                .getAsJsonObject()
                .getAsJsonArray("files");
            System.out.println("✓ Found " + fileArray.size() + " file(s)");
            
            // Get file details
            System.out.println("\nGetting file details...");
            HypzClient.HypzResponse fileDetails = client.files().get(fileId);
            JsonObject fileData = fileDetails.getData().getAsJsonObject();
            System.out.println("✓ File: " + fileData.get("original_name").getAsString());
            System.out.println("  Size: " + fileData.get("size").getAsLong() + " bytes");
            System.out.println("  Type: " + fileData.get("mime_type").getAsString());
            
            // Get public URL
            String publicUrl = client.files().getPublicUrl(fileId);
            System.out.println("  Public URL: " + publicUrl);
            
            // Download file
            System.out.println("\nDownloading file...");
            byte[] downloaded = client.files().downloadBytes(fileId);
            System.out.println("✓ Downloaded " + downloaded.length + " bytes");
            System.out.println("  Content: " + new String(downloaded));
            
            // ============================================
            // 3. API KEY OPERATIONS
            // ============================================
            System.out.println("\n=== API KEY OPERATIONS ===\n");
            
            // Create API key
            System.out.println("Creating API key...");
            String[] permissions = {"files:read", "files:write", "files:delete"};
            HypzClient.HypzResponse createKey = client.apiKeys()
                .create("Java SDK Test Key", permissions, 0);
            System.out.println("✓ " + createKey.getMessage());
            
            String keyId = createKey.getData()
                .getAsJsonObject()
                .get("id")
                .getAsString();
            
            // List API keys
            System.out.println("\nListing API keys...");
            HypzClient.HypzResponse keys = client.apiKeys().list();
            JsonArray keyArray = keys.getData()
                .getAsJsonObject()
                .getAsJsonArray("keys");
            System.out.println("✓ Found " + keyArray.size() + " API key(s)");
            
            // Delete test API key
            System.out.println("\nDeleting test API key...");
            HypzClient.HypzResponse deleteKey = client.apiKeys().delete(keyId);
            System.out.println("✓ " + deleteKey.getMessage());
            
            // ============================================
            // 4. USAGE STATISTICS
            // ============================================
            System.out.println("\n=== USAGE STATISTICS ===\n");
            
            System.out.println("Getting current usage...");
            HypzClient.HypzResponse usage = client.usage().getCurrent();
            JsonObject usageData = usage.getData().getAsJsonObject();
            
            long storageUsed = usageData.get("storage_used").getAsLong();
            long bandwidthUsed = usageData.get("bandwidth_used").getAsLong();
            int apiCalls = usageData.get("api_calls").getAsInt();
            
            System.out.println("✓ Current Usage:");
            System.out.println("  Storage: " + formatBytes(storageUsed));
            System.out.println("  Bandwidth: " + formatBytes(bandwidthUsed));
            System.out.println("  API Calls: " + apiCalls);
            
            // ============================================
            // 5. CLEANUP
            // ============================================
            System.out.println("\n=== CLEANUP ===\n");
            
            // Delete file
            System.out.println("Deleting test file...");
            HypzClient.HypzResponse deleteFile = client.files().delete(fileId);
            System.out.println("✓ " + deleteFile.getMessage());
            
            // Delete bucket
            System.out.println("\nDeleting test bucket...");
            HypzClient.HypzResponse deleteBucket = client.buckets().delete(bucketId);
            System.out.println("✓ " + deleteBucket.getMessage());
            
            System.out.println("\n=== ALL TESTS COMPLETED SUCCESSFULLY ===\n");
            
        } catch (IOException e) {
            System.err.println("❌ Network error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("❌ Thread interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Format bytes to human-readable format
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
