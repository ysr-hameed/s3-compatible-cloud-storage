/**
 * Hypz Cloud Storage SDK - Complete Implementation
 * 
 * Full-featured SDK for Hypz Cloud Storage Platform
 * Supports: Buckets, Files, API Keys, Usage, Subscriptions, Webhooks, and more
 * 
 * @version 2.0.0
 * @author Hypz Team
 * @license MIT
 */

const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const EventEmitter = require('events');

/**
 * Main Hypz SDK Client
 */
class HypzSDK extends EventEmitter {
  /**
   * Initialize Hypz SDK
   * @param {string} apiKey - Your Hypz API key
   * @param {object} options - Configuration options
   */
  constructor(apiKey, options = {}) {
    super();
    
    // Support constructor(HYPZ_API_KEY, options) or constructor({ apiKey, baseURL, jwt, ... })
    if (apiKey && typeof apiKey === 'object') {
      options = apiKey || {};
      apiKey = options.apiKey;
    }

    this.apiKey = apiKey || null;
    this.jwt = options.jwt || options.token || null; // allow dashboard JWT usage
    if (!this.apiKey && !this.jwt) {
      throw new Error('API key or JWT token is required');
    }

    this.baseURL = options.baseURL || 'http://localhost:5000/api/v1';
    this.timeout = options.timeout || 30000;
    this.retries = options.retries || 3;
    this.retryDelay = options.retryDelay || 1000;
    
    // Initialize axios client
    const headers = { 'Content-Type': 'application/json' };
    if (this.apiKey) headers['X-API-Key'] = this.apiKey;
    if (this.jwt) headers['Authorization'] = `Bearer ${this.jwt}`;

    this.client = axios.create({ baseURL: this.baseURL, timeout: this.timeout, headers });
    
    // Add response interceptor for error handling
    this.client.interceptors.response.use(
      response => response,
      error => this._handleError(error)
    );
    
    // Initialize resource managers
    this.auth = new AuthManager(this);
    this.buckets = new BucketManager(this);
    this.files = new FileManager(this);
    this.apiKeys = new APIKeyManager(this);
    this.usage = new UsageManager(this);
    this.subscriptions = new SubscriptionManager(this);
    this.plans = new PlanManager(this);
    this.user = new UserManager(this);
    this.notifications = new NotificationManager(this);
    this.webhooks = new WebhookManager(this);
    this.admin = new AdminManager(this);
  }
  
  /**
   * Make HTTP request
   * @private
   */
  async _request(method, endpoint, data = null, config = {}) {
    const requestConfig = {
      method,
      url: endpoint,
      ...config
    };
    
    if (data) {
      if (method === 'GET') {
        requestConfig.params = data;
      } else {
        requestConfig.data = data;
      }
    }
    
    try {
      const response = await this.client.request(requestConfig);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
  
  /**
   * Handle errors with retry logic
   * @private
   */
  async _handleError(error) {
    if (error.config && error.config.__retryCount < this.retries) {
      error.config.__retryCount = error.config.__retryCount || 0;
      error.config.__retryCount++;
      
      // Wait before retry
      await new Promise(resolve => setTimeout(resolve, this.retryDelay));
      
      return this.client.request(error.config);
    }
    
    // Format error
    const hypzError = new HypzError(
      error.response?.data?.message || error.message,
      error.response?.status,
      error.response?.data
    );
    
    this.emit('error', hypzError);
    throw hypzError;
  }
  
  /**
   * Test connection to API
   */
  async testConnection() {
    try {
      const response = await this._request('GET', '/buckets', { limit: 1 });
      return { success: true, message: 'Connection successful' };
    } catch (error) {
      return { success: false, message: error.message };
    }
  }
}

/**
 * Auth Manager - Authentication utilities
 */
class AuthManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  /**
   * Get current authenticated user
   */
  async getCurrentUser() {
    return this.sdk._request('GET', '/auth/me');
  }
}

/**
 * Bucket Manager - Manage storage buckets
 */
class BucketManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  /**
   * Create a new bucket
   */
  async create(data) {
    return this.sdk._request('POST', '/buckets', data);
  }
  
  /**
   * List all buckets
   */
  async list(params = {}) {
    return this.sdk._request('GET', '/buckets', params);
  }
  
  /**
   * Get bucket by ID
   */
  async get(bucketId) {
    return this.sdk._request('GET', `/buckets/${bucketId}`);
  }
  
  /**
   * Update bucket
   */
  async update(bucketId, data) {
    return this.sdk._request('PUT', `/buckets/${bucketId}`, data);
  }
  
  /**
   * Delete bucket
   */
  async delete(bucketId, force = false) {
    const url = `/buckets/${bucketId}${force ? '?force=true' : ''}`;
    return this.sdk._request('DELETE', url);
  }
  
  /**
   * Get bucket statistics
   */
  async getStats(bucketId) {
    return this.sdk._request('GET', `/buckets/${bucketId}/stats`);
  }

  async stats(bucketId) {
    return this.getStats(bucketId);
  }
}

/**
 * File Manager - Manage files and uploads
 */
class FileManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  /**
   * Upload file to bucket
   */
  async upload(bucketId, file, options = {}) {
    // Support signature: upload({ bucketId, file, fileName, isPublic, tags, metadata, onProgress })
    if (bucketId && typeof bucketId === 'object' && !file) {
      const input = bucketId;
      bucketId = input.bucketId;
      file = input.file;
      options = {
        filename: input.fileName || input.filename,
        isPublic: input.isPublic,
        tags: input.tags,
        metadata: input.metadata,
        onProgress: input.onProgress
      };
    }
    
    const formData = new FormData();
    
    // Handle file input
    if (typeof file === 'string') {
      // File path
      formData.append('file', fs.createReadStream(file));
    } else if (file.pipe) {
      // Stream
      formData.append('file', file);
    } else if (Buffer.isBuffer(file)) {
      // Buffer
      formData.append('file', file, { filename: options.filename || options.fileName || 'file' });
    } else {
      throw new Error('Invalid file input');
    }
    
    // Add options (note: isPublic is deprecated - file visibility matches bucket)
    if (options.tags) {
      formData.append('tags', JSON.stringify(options.tags));
    }
    if (options.metadata) {
      formData.append('metadata', JSON.stringify(options.metadata));
    }
    
    const config = {
      headers: formData.getHeaders(),
      maxBodyLength: Infinity,
      maxContentLength: Infinity
    };
    
    // Add progress callback
    if (options.onProgress) {
      config.onUploadProgress = (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        options.onProgress(percentCompleted, progressEvent);
      };
    }
    
    return this.sdk._request('POST', `/files/${bucketId}/upload`, formData, config);
  }
  
  /**
   * List files in bucket
   */
  async list(bucketId, params = {}) {
    return this.sdk._request('GET', `/files/${bucketId}/files`, params);
  }
  
  /**
   * Get file details
   */
  async get(fileId) {
    return this.sdk._request('GET', `/files/file/${fileId}`);
  }
  
  /**
   * Update file metadata
   */
  /**
   * Update file metadata
   * Note: Cannot change isPublic - file visibility is inherited from bucket
   * @param {string} fileId - File ID
   * @param {object} data - Update data (tags, metadata only)
   */
  async update(fileId, data) {
    return this.sdk._request('PATCH', `/files/file/${fileId}`, data);
  }
  
  /**
   * Delete file
   */
  async delete(fileId) {
    return this.sdk._request('DELETE', `/files/file/${fileId}`);
  }
  
  /**
   * Get download URL for file
   */
  getDownloadURL(fileId) {
    return `${this.sdk.baseURL}/files/file/${fileId}/download`;
  }
  
  /**
   * Generate signed download URL for private files
   * @param {string} fileId
   * @param {number} expiresInSeconds up to 7 days (604800)
   */
  async getSignedURL(fileId, expiresInSeconds = 3600) {
    const body = { expiresIn: Math.min(Math.max(1, Math.floor(expiresInSeconds)), 604800) };
    const res = await this.sdk._request('POST', `/files/file/${fileId}/signed-url`, body);
    return res; // Return full response with metadata
  }
  
  /**
   * Download file to stream or buffer
   */
  async download(fileId, options = {}) {
    const config = {
      responseType: options.stream ? 'stream' : 'arraybuffer',
      headers: {
        'X-API-Key': this.sdk.apiKey
      }
    };
    
    if (options.onProgress) {
      config.onDownloadProgress = (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        options.onProgress(percentCompleted, progressEvent);
      };
    }
    
    const response = await axios.get(this.getDownloadURL(fileId), config);
    
    if (options.saveTo) {
      // Save to file
      const writer = fs.createWriteStream(options.saveTo);
      if (options.stream) {
        response.data.pipe(writer);
      } else {
        writer.write(Buffer.from(response.data));
        writer.end();
      }
      return new Promise((resolve, reject) => {
        writer.on('finish', () => resolve({ saved: options.saveTo }));
        writer.on('error', reject);
      });
    }
    
    return response.data;
  }

  /**
   * Initiate a direct-to-B2 presigned upload
   * @param {Object} options
   * @param {string} options.bucketId
   * @param {string} options.fileName
   * @param {number} [options.fileSize]
   * @param {string} [options.mimeType]
   * @param {Array<string>} [options.tags]
   * @param {Object} [options.metadata]
   */
  async initiatePresignedUpload(options) {
    const {
      bucketId,
      fileName,
      fileSize,
      mimeType,
      tags,
      metadata
    } = options || {};

    if (!bucketId) {
      throw new Error('bucketId is required');
    }
    if (!fileName) {
      throw new Error('fileName is required');
    }

    const payload = {
      filename: fileName,
      mimeType: mimeType || 'application/octet-stream',
      size: typeof fileSize === 'number' ? fileSize : undefined,
      tags,
      metadata
    };

    Object.keys(payload).forEach((key) => {
      if (payload[key] === undefined || payload[key] === null) {
        delete payload[key];
      }
    });

    return this.sdk._request('POST', `/files/${bucketId}/files/presigned`, payload);
  }

  /**
   * Complete a presigned upload after uploading to B2
   * @param {string} fileId
   * @param {Object} payload
   */
  async completePresignedUpload(fileId, payload) {
    if (!fileId) {
      throw new Error('fileId is required');
    }
    if (!payload || !payload.b2FileId) {
      throw new Error('payload.b2FileId is required');
    }

    return this.sdk._request('POST', `/files/file/${fileId}/complete`, payload);
  }

  /**
   * Bulk delete files (up to 100 files)
   */
  async bulkDelete(fileIds) {
    if (!Array.isArray(fileIds) || fileIds.length === 0) {
      throw new Error('fileIds must be a non-empty array');
    }
    if (fileIds.length > 100) {
      throw new Error('Maximum 100 files can be deleted at once');
    }
    return this.sdk._request('POST', '/files/bulk/delete', { fileIds });
  }

  /**
   * Bulk update files (up to 100 files)
   * Note: Cannot change isPublic - file visibility is inherited from bucket
   */
  async bulkUpdate(data) {
    const { fileIds, tags, metadata } = data;
    if (!Array.isArray(fileIds) || fileIds.length === 0) {
      throw new Error('fileIds must be a non-empty array');
    }
    if (fileIds.length > 100) {
      throw new Error('Maximum 100 files can be updated at once');
    }
    return this.sdk._request('POST', '/files/bulk/update', { fileIds, tags, metadata });
  }

  /**
   * Bulk download - get download URLs for multiple files (up to 50 files)
   */
  async bulkDownload(fileIds) {
    if (!Array.isArray(fileIds) || fileIds.length === 0) {
      throw new Error('fileIds must be a non-empty array');
    }
    if (fileIds.length > 50) {
      throw new Error('Maximum 50 files can be downloaded at once');
    }
    return this.sdk._request('POST', '/files/bulk/download', { fileIds });
  }

  /**
   * Bulk move files to another bucket (up to 100 files)
   */
  async bulkMove(data) {
    const { fileIds, targetBucketId } = data;
    if (!Array.isArray(fileIds) || fileIds.length === 0) {
      throw new Error('fileIds must be a non-empty array');
    }
    if (!targetBucketId) {
      throw new Error('targetBucketId is required');
    }
    if (fileIds.length > 100) {
      throw new Error('Maximum 100 files can be moved at once');
    }
    return this.sdk._request('POST', '/files/bulk/move', { fileIds, targetBucketId });
  }

  /**
   * Bulk upload files (up to 20 files)
   * @param {Object} options - Upload options
   * @param {string} options.bucketId - Target bucket ID
   * @param {Array} options.files - Array of file objects {file: Buffer|Stream|Path, filename: string}
   * @param {boolean} options.isPublic - Make files public
   * @param {Array} options.tags - Tags for all files
   * @param {Object} options.metadata - Metadata for all files
   */
  async bulkUpload(options) {
    const { bucketId, files, isPublic, tags, metadata } = options;
    
    if (!bucketId) {
      throw new Error('bucketId is required');
    }
    if (!Array.isArray(files) || files.length === 0) {
      throw new Error('files must be a non-empty array');
    }
    if (files.length > 20) {
      throw new Error('Maximum 20 files can be uploaded at once');
    }

    const formData = new FormData();
    
    // Add each file
    files.forEach((fileObj, index) => {
      const { file, filename } = fileObj;
      if (typeof file === 'string') {
        // File path
        formData.append('files', fs.createReadStream(file), filename || file);
      } else if (file.pipe) {
        // Stream
        formData.append('files', file, filename || 'file');
      } else if (Buffer.isBuffer(file)) {
        // Buffer
        formData.append('files', file, { filename: filename || `file-${index}` });
      } else {
        throw new Error(`Invalid file input at index ${index}`);
      }
    });

    // Add common options (note: isPublic is deprecated - file visibility matches bucket)
    if (tags) {
      formData.append('tags', JSON.stringify(tags));
    }
    if (metadata) {
      formData.append('metadata', JSON.stringify(metadata));
    }

    const config = {
      headers: formData.getHeaders(),
      maxBodyLength: Infinity,
      maxContentLength: Infinity
    };

    return this.sdk._request('POST', `/files/${bucketId}/bulk-upload`, formData, config);
  }
}

/**
 * API Key Manager - Manage API keys
 */
class APIKeyManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async create(data) {
    return this.sdk._request('POST', '/api-keys', data);
  }
  
  async list(params = {}) {
    return this.sdk._request('GET', '/api-keys', params);
  }
  
  async get(keyId) {
    return this.sdk._request('GET', `/api-keys/${keyId}`);
  }
  
  async update(keyId, data) {
    return this.sdk._request('PUT', `/api-keys/${keyId}`, data);
  }
  
  async delete(keyId) {
    return this.sdk._request('DELETE', `/api-keys/${keyId}`);
  }

  async revoke(keyId) {
    return this.delete(keyId);
  }
  
  async regenerate(keyId) {
    return this.sdk._request('POST', `/api-keys/${keyId}/regenerate`);
  }
}

/**
 * Usage Manager - Track API usage and storage
 */
class UsageManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async current() {
    return this.sdk._request('GET', '/usage/current');
  }
  
  async history(params = {}) {
    return this.sdk._request('GET', '/usage/history', params);
  }
  
  async analytics(params = {}) {
    return this.sdk._request('GET', '/usage/analytics', params);
  }
}

/**
 * Subscription Manager - Manage billing and subscriptions
 */
class SubscriptionManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async current() {
    return this.sdk._request('GET', '/subscriptions/current');
  }
  
  async subscribe(planId) {
    return this.sdk._request('POST', '/subscriptions', { planId });
  }
  
  async cancel() {
    return this.sdk._request('POST', '/subscriptions/cancel');
  }
  
  async updatePaymentMethod(data) {
    return this.sdk._request('PUT', '/subscriptions/payment-method', data);
  }
}

/**
 * Plan Manager - View available plans
 */
class PlanManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async list() {
    return this.sdk._request('GET', '/plans');
  }
  
  async get(planId) {
    return this.sdk._request('GET', `/plans/${planId}`);
  }
}

/**
 * User Manager - Manage user profile
 */
class UserManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async profile() {
    return this.sdk._request('GET', '/user/profile');
  }
  
  async updateProfile(data) {
    return this.sdk._request('PUT', '/user/profile', data);
  }
  
  async changePassword(data) {
    return this.sdk._request('PUT', '/user/change-password', data);
  }
  
  async notificationPreferences() {
    return this.sdk._request('GET', '/user/notifications');
  }
  
  async updateNotificationPreferences(data) {
    return this.sdk._request('PUT', '/user/notifications', data);
  }
}

/**
 * Notification Manager - Manage notifications
 */
class NotificationManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async list(params = {}) {
    return this.sdk._request('GET', '/notifications', params);
  }
  
  async markAsRead(notificationId) {
    return this.sdk._request('PUT', `/notifications/${notificationId}/read`);
  }
  
  async markAllAsRead() {
    return this.sdk._request('PUT', '/notifications/read-all');
  }
  
  async delete(notificationId) {
    return this.sdk._request('DELETE', `/notifications/${notificationId}`);
  }
}

/**
 * Webhook Manager - Manage webhooks
 */
class WebhookManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async create(data) {
    return this.sdk._request('POST', '/webhooks', data);
  }
  
  async list() {
    return this.sdk._request('GET', '/webhooks');
  }
  
  async get(webhookId) {
    return this.sdk._request('GET', `/webhooks/${webhookId}`);
  }
  
  async update(webhookId, data) {
    return this.sdk._request('PUT', `/webhooks/${webhookId}`, data);
  }
  
  async delete(webhookId) {
    return this.sdk._request('DELETE', `/webhooks/${webhookId}`);
  }
  
  async test(webhookId) {
    return this.sdk._request('POST', `/webhooks/${webhookId}/test`);
  }
}

/**
 * Admin Manager - Admin operations (requires admin role)
 */
class AdminManager {
  constructor(sdk) {
    this.sdk = sdk;
  }
  
  async stats() {
    return this.sdk._request('GET', '/admin/stats');
  }
  
  async users(params = {}) {
    return this.sdk._request('GET', '/admin/users', params);
  }
  
  async updateUserStatus(userId, isActive) {
    return this.sdk._request('PATCH', `/admin/users/${userId}/status`, { isActive });
  }
  
  async logs(params = {}) {
    return this.sdk._request('GET', '/admin/logs', params);
  }
  
  async settings() {
    return this.sdk._request('GET', '/admin/settings');
  }
  
  async updateSetting(key, value) {
    return this.sdk._request('PUT', `/admin/settings/${key}`, { value });
  }
}

/**
 * Custom Error Class
 */
class HypzError extends Error {
  constructor(message, statusCode, data) {
    super(message);
    this.name = 'HypzError';
    this.statusCode = statusCode;
    this.data = data;
    Error.captureStackTrace(this, this.constructor);
  }
}

// Export SDK
module.exports = {
  HypzSDK,
  HypzError
};

// Convenience export
module.exports.default = HypzSDK;
