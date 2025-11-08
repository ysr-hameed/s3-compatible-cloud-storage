// Type definitions for @hypz/sdk
// Project: https://github.com/ysr-hameed/hypz
// Definitions by: Hypz Team

export interface HypzConfig {
  apiKey: string;
  baseURL?: string;
}

export interface BucketOptions {
  name: string;
  isPublic: boolean;
}

export interface Bucket {
  id: string;
  name: string;
  isPublic: boolean;
  userId: string;
  createdAt: string;
  updatedAt: string;
  fileCount?: number;
  totalSize?: number;
}

export interface FileUploadOptions {
  bucketId: string;
  file: Buffer | Blob | File;
  fileName: string;
  isPublic?: boolean;
}

export interface FileInfo {
  id: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  bucketId: string;
  userId: string;
  url: string;
  isPublic: boolean;
  uploadedAt: string;
}

export interface APIKeyOptions {
  name: string;
  permissions?: string[];
  expiresAt?: string;
}

export interface APIKey {
  id: string;
  name: string;
  key: string;
  permissions: string[];
  userId: string;
  createdAt: string;
  expiresAt?: string;
  lastUsedAt?: string;
}

export interface UsageInfo {
  storageUsed: number;
  storageLimit: number;
  bandwidthUsed: number;
  bandwidthLimit: number;
  filesCount: number;
  bucketsCount: number;
}

export interface UsageHistoryEntry {
  date: string;
  storageUsed: number;
  bandwidthUsed: number;
}

export class HypzError extends Error {
  statusCode: number;
  response: any;
  constructor(message: string, statusCode: number, response?: any);
}

export class BucketManager {
  constructor(sdk: HypzSDK);
  create(options: BucketOptions): Promise<Bucket>;
  list(): Promise<Bucket[]>;
  get(bucketId: string): Promise<Bucket>;
  update(bucketId: string, updates: Partial<BucketOptions>): Promise<Bucket>;
  delete(bucketId: string): Promise<void>;
}

export class FileManager {
  constructor(sdk: HypzSDK);
  upload(options: FileUploadOptions): Promise<FileInfo>;
  list(bucketId: string): Promise<{ files: FileInfo[]; total: number }>;
  get(fileId: string): Promise<FileInfo>;
  download(fileId: string): Promise<any>;
  delete(fileId: string): Promise<void>;
  getPublicUrl(fileId: string): string;
}

export class APIKeyManager {
  constructor(sdk: HypzSDK);
  create(options: APIKeyOptions): Promise<APIKey>;
  list(): Promise<APIKey[]>;
  get(keyId: string): Promise<APIKey>;
  revoke(keyId: string): Promise<void>;
}

export class UsageManager {
  constructor(sdk: HypzSDK);
  getCurrent(): Promise<UsageInfo>;
  getHistory(): Promise<UsageHistoryEntry[]>;
}

export class HypzSDK {
  buckets: BucketManager;
  files: FileManager;
  apiKeys: APIKeyManager;
  usage: UsageManager;

  constructor(config: HypzConfig);
}

export { HypzSDK as default };
