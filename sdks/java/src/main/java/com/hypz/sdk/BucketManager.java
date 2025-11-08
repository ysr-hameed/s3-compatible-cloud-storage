package com.hypz.sdk;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Bucket operations manager
 */
public class BucketManager {
    
    private final HypzClient client;
    
    BucketManager(HypzClient client) {
        this.client = client;
    }
    
    /**
     * Create a new bucket
     * 
     * @param name Bucket name
     * @param visibility "public" or "private"
     * @param description Optional description
     * @return Bucket creation response
     */
    public HypzClient.HypzResponse create(String name, String visibility, String description) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("visibility", visibility);
        if (description != null) {
            data.put("description", description);
        }
        
        return client.post("/buckets", data);
    }
    
    /**
     * Create a public bucket
     */
    public HypzClient.HypzResponse createPublic(String name) throws IOException {
        return create(name, "public", null);
    }
    
    /**
     * Create a private bucket
     */
    public HypzClient.HypzResponse createPrivate(String name) throws IOException {
        return create(name, "private", null);
    }
    
    /**
     * List all buckets
     * 
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 10)
     * @return List of buckets
     */
    public HypzClient.HypzResponse list(int page, int limit) throws IOException {
        return client.get("/buckets?page=" + page + "&limit=" + limit);
    }
    
    /**
     * List all buckets with default pagination
     */
    public HypzClient.HypzResponse list() throws IOException {
        return list(1, 10);
    }
    
    /**
     * Get bucket by ID
     * 
     * @param bucketId Bucket ID
     * @return Bucket details
     */
    public HypzClient.HypzResponse get(String bucketId) throws IOException {
        return client.get("/buckets/" + bucketId);
    }
    
    /**
     * Update bucket
     * 
     * @param bucketId Bucket ID
     * @param updates Map of fields to update
     * @return Update response
     */
    public HypzClient.HypzResponse update(String bucketId, Map<String, Object> updates) throws IOException {
        return client.put("/buckets/" + bucketId, updates);
    }
    
    /**
     * Update bucket name
     */
    public HypzClient.HypzResponse updateName(String bucketId, String newName) throws IOException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        return update(bucketId, updates);
    }
    
    /**
     * Update bucket visibility
     */
    public HypzClient.HypzResponse updateVisibility(String bucketId, String visibility) throws IOException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("visibility", visibility);
        return update(bucketId, updates);
    }
    
    /**
     * Delete bucket
     * 
     * @param bucketId Bucket ID
     * @return Deletion response
     */
    public HypzClient.HypzResponse delete(String bucketId) throws IOException {
        return client.delete("/buckets/" + bucketId);
    }
}
