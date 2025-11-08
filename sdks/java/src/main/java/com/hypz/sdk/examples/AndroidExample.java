package com.hypz.sdk.examples;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hypz.sdk.*;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Example Android Activity using Hypz SDK
 * 
 * Add to build.gradle:
 * implementation 'com.hypz:hypz-sdk:1.0.0'
 * implementation 'com.squareup.okhttp3:okhttp:4.12.0'
 * implementation 'com.google.code.gson:gson:2.10.1'
 */
public class AndroidExample extends AppCompatActivity {
    
    private HypzClient hypzClient;
    private Executor backgroundExecutor;
    private TextView statusText;
    private String bucketId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize executor for background tasks
        backgroundExecutor = Executors.newSingleThreadExecutor();
        
        // Initialize Hypz SDK
        hypzClient = new HypzClient.Builder()
            .apiKey("sk_live_your_api_key_here") // Store in BuildConfig or secrets
            .baseUrl("https://api.hypz.io/api/v1")
            .connectTimeout(30)
            .readTimeout(60)
            .writeTimeout(60)
            .build();
        
        // UI elements
        statusText = findViewById(R.id.statusText);
        Button createBucketBtn = findViewById(R.id.createBucketBtn);
        Button uploadFileBtn = findViewById(R.id.uploadFileBtn);
        Button listFilesBtn = findViewById(R.id.listFilesBtn);
        Button getUsageBtn = findViewById(R.id.getUsageBtn);
        
        // Button listeners
        createBucketBtn.setOnClickListener(v -> createBucket());
        uploadFileBtn.setOnClickListener(v -> uploadFile());
        listFilesBtn.setOnClickListener(v -> listFiles());
        getUsageBtn.setOnClickListener(v -> getUsage());
    }
    
    /**
     * Create a bucket
     */
    private void createBucket() {
        updateStatus("Creating bucket...");
        
        backgroundExecutor.execute(() -> {
            try {
                HypzClient.HypzResponse response = hypzClient.buckets()
                    .createPublic("android-app-bucket");
                
                if (response.isSuccess()) {
                    bucketId = response.getData()
                        .getAsJsonObject()
                        .get("id")
                        .getAsString();
                    
                    runOnUiThread(() -> {
                        updateStatus("✓ Bucket created: " + bucketId);
                        showToast("Bucket created successfully!");
                    });
                } else {
                    runOnUiThread(() -> 
                        updateStatus("❌ Error: " + response.getMessage()));
                }
            } catch (Exception e) {
                runOnUiThread(() -> 
                    updateStatus("❌ Network error: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Upload a file
     */
    private void uploadFile() {
        if (bucketId == null) {
            showToast("Please create a bucket first");
            return;
        }
        
        updateStatus("Uploading file...");
        
        backgroundExecutor.execute(() -> {
            try {
                // Create a test file
                File tempFile = File.createTempFile("test", ".txt", getCacheDir());
                String content = "Uploaded from Android at " + System.currentTimeMillis();
                java.nio.file.Files.write(tempFile.toPath(), content.getBytes());
                
                HypzClient.HypzResponse response = hypzClient.files()
                    .upload(bucketId, tempFile);
                
                tempFile.delete();
                
                if (response.isSuccess()) {
                    String fileId = response.getData()
                        .getAsJsonObject()
                        .get("id")
                        .getAsString();
                    
                    runOnUiThread(() -> {
                        updateStatus("✓ File uploaded: " + fileId);
                        showToast("Upload successful!");
                    });
                } else {
                    runOnUiThread(() -> 
                        updateStatus("❌ Error: " + response.getMessage()));
                }
            } catch (Exception e) {
                runOnUiThread(() -> 
                    updateStatus("❌ Upload error: " + e.getMessage()));
            }
        });
    }
    
    /**
     * List files in bucket
     */
    private void listFiles() {
        if (bucketId == null) {
            showToast("Please create a bucket first");
            return;
        }
        
        updateStatus("Loading files...");
        
        backgroundExecutor.execute(() -> {
            try {
                HypzClient.HypzResponse response = hypzClient.files()
                    .list(bucketId);
                
                if (response.isSuccess()) {
                    com.google.gson.JsonArray files = response.getData()
                        .getAsJsonObject()
                        .getAsJsonArray("files");
                    
                    int fileCount = files.size();
                    
                    runOnUiThread(() -> {
                        StringBuilder status = new StringBuilder();
                        status.append("✓ Found ").append(fileCount).append(" file(s)\n\n");
                        
                        for (int i = 0; i < files.size(); i++) {
                            com.google.gson.JsonObject file = files.get(i).getAsJsonObject();
                            String name = file.get("original_name").getAsString();
                            long size = file.get("size").getAsLong();
                            status.append("• ").append(name)
                                  .append(" (").append(formatBytes(size)).append(")\n");
                        }
                        
                        updateStatus(status.toString());
                    });
                } else {
                    runOnUiThread(() -> 
                        updateStatus("❌ Error: " + response.getMessage()));
                }
            } catch (Exception e) {
                runOnUiThread(() -> 
                    updateStatus("❌ Network error: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get usage statistics
     */
    private void getUsage() {
        updateStatus("Loading usage stats...");
        
        backgroundExecutor.execute(() -> {
            try {
                HypzClient.HypzResponse response = hypzClient.usage()
                    .getCurrent();
                
                if (response.isSuccess()) {
                    com.google.gson.JsonObject data = response.getData().getAsJsonObject();
                    
                    long storageUsed = data.get("storage_used").getAsLong();
                    long bandwidthUsed = data.get("bandwidth_used").getAsLong();
                    int apiCalls = data.get("api_calls").getAsInt();
                    
                    runOnUiThread(() -> {
                        String status = "✓ Current Usage:\n\n" +
                            "Storage: " + formatBytes(storageUsed) + "\n" +
                            "Bandwidth: " + formatBytes(bandwidthUsed) + "\n" +
                            "API Calls: " + apiCalls;
                        updateStatus(status);
                    });
                } else {
                    runOnUiThread(() -> 
                        updateStatus("❌ Error: " + response.getMessage()));
                }
            } catch (Exception e) {
                runOnUiThread(() -> 
                    updateStatus("❌ Network error: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Update status text on UI thread
     */
    private void updateStatus(String message) {
        runOnUiThread(() -> statusText.setText(message));
    }
    
    /**
     * Show toast message
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
