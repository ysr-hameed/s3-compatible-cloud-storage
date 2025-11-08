package com.hypz.sdk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API Key operations manager
 */
public class ApiKeyManager {
    
    private final HypzClient client;
    
    ApiKeyManager(HypzClient client) {
        this.client = client;
    }
    
    /**
     * Create a new API key
     * 
     * @param name Key name
     * @param permissions Array of permissions
     * @param expiresInDays Number of days until expiration (0 for no expiration)
     * @return API key creation response
     */
    public HypzClient.HypzResponse create(String name, String[] permissions, int expiresInDays) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("permissions", permissions);
        if (expiresInDays > 0) {
            data.put("expiresInDays", expiresInDays);
        }
        
        return client.post("/api-keys", data);
    }
    
    /**
     * Create API key with default permissions (read/write)
     */
    public HypzClient.HypzResponse create(String name) throws IOException {
        String[] defaultPermissions = {"files:read", "files:write", "files:delete"};
        return create(name, defaultPermissions, 0);
    }
    
    /**
     * List all API keys
     * 
     * @return List of API keys
     */
    public HypzClient.HypzResponse list() throws IOException {
        return client.get("/api-keys");
    }
    
    /**
     * Get API key details
     * 
     * @param keyId Key ID
     * @return API key details
     */
    public HypzClient.HypzResponse get(String keyId) throws IOException {
        return client.get("/api-keys/" + keyId);
    }
    
    /**
     * Update API key
     * 
     * @param keyId Key ID
     * @param name New name
     * @param permissions New permissions
     * @return Update response
     */
    public HypzClient.HypzResponse update(String keyId, String name, String[] permissions) throws IOException {
        Map<String, Object> updates = new HashMap<>();
        if (name != null) {
            updates.put("name", name);
        }
        if (permissions != null) {
            updates.put("permissions", permissions);
        }
        
        return client.put("/api-keys/" + keyId, updates);
    }
    
    /**
     * Delete API key
     * 
     * @param keyId Key ID
     * @return Deletion response
     */
    public HypzClient.HypzResponse delete(String keyId) throws IOException {
        return client.delete("/api-keys/" + keyId);
    }
}
