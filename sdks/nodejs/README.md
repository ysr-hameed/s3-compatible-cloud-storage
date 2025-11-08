Beginner‑friendly, batteries‑included Node.js SDK for the Hypz Cloud Storage Platform. Build apps that create buckets, upload files, generate time‑limited links for private content, and track usage—using a modern, promise‑based API with great DX.

 Package: `@hypz/sdk`
 Status: Production‑ready
 Auth: API Key (server/serverless), JWT (dashboard/services)
 Private access: Signed URLs (max expiry: 7 days)

## Table of contents

- Getting started (install, setup, hello world)
- Authentication (API key vs JWT; when to use which)
- Buckets (create, list, get, update, delete, stats)
- Files (upload variants, list, get, download, delete, metadata, tags)
- Private access (signed URLs ≤ 7 days)
- Downloads (buffer, stream, save to disk)
- Usage (current, history, analytics)
- Error handling and retries
- Timeouts and configuration
- TypeScript types and intellisense
- Examples by environment (Node, Serverless, CLI)
- Troubleshooting and FAQs
- Publishing note (.tgz explained)

## 1) Getting started

### Install

```bash
npm install @hypz/sdk
# or
pnpm add @hypz/sdk
# or
yarn add @hypz/sdk
```

### Initialize

Two constructor forms are supported:

```js
const { HypzSDK } = require('@hypz/sdk');

// Object form (recommended)
const hypz = new HypzSDK({
  apiKey: process.env.HYPZ_API_KEY,      // or omit apiKey and set jwt
  jwt: process.env.HYPZ_JWT,             // optional; Authorization: Bearer <jwt>
  baseURL: 'http://localhost:5000/api/v1',
  timeout: 30_000,                       // ms
  retries: 3,
  retryDelay: 1_000
});

// Positional form
// const hypz = new HypzSDK('sk_live_...', { baseURL: 'http://localhost:5000/api/v1' });
```

### Hello world (create bucket → upload → get signed URL)

```js
const fs = require('fs');

const bucket = await hypz.buckets.create({ name: 'docs-demo', visibility: 'private' });
const buffer = Buffer.from('hello hypz');
const file = await hypz.files.upload({ bucketId: bucket.id, file: buffer, fileName: 'hello.txt' });
const url = await hypz.files.getSignedURL(file.id, 3600); // 1 hour (max 7 days)
logger.log('Shareable link:', url);
```

---

## 2) Authentication

- API Key (recommended for server ↔ server): sends `X-API-Key`
- JWT (dashboard/services): sends `Authorization: Bearer <jwt>`

The SDK accepts either `apiKey` or `jwt`. Provide at least one.

```js
const hypz = new HypzSDK({ apiKey: 'sk_live...', baseURL: 'http://localhost:5000/api/v1' });
// or
const hypzJwt = new HypzSDK({ jwt: 'eyJhbGciOiJI...', baseURL: 'http://localhost:5000/api/v1' });
```

---

## 3) Buckets

```js
// Create (visibility: 'private' | 'public')
await hypz.buckets.create({ name: 'images', visibility: 'public', description: 'Marketing images' });

// List
const list = await hypz.buckets.list({ page: 1, limit: 20 });

// Get
const bucket = await hypz.buckets.get(bucketId);

// Update
await hypz.buckets.update(bucketId, { name: 'images-2025', visibility: 'private' });

// Delete
await hypz.buckets.delete(bucketId);

// Stats (if supported by backend)
await hypz.buckets.stats(bucketId);
```

Tips:
- Bucket names: lowercase letters, numbers, hyphens. 3–63 chars.
- Public buckets allow direct public downloads.
- Private buckets require auth or signed URLs.

---

## 4) Files

### Upload variants

```js
const path = require('path');
const fs = require('fs');

// Buffer
await hypz.files.upload({ bucketId, file: Buffer.from('hello'), fileName: 'hello.txt' });

// Stream
await hypz.files.upload(bucketId, fs.createReadStream('/tmp/photo.jpg'), { filename: 'photo.jpg' });

// File path (Node only)
await hypz.files.upload(bucketId, '/tmp/report.pdf', { filename: 'report.pdf', metadata: { year: 2025 } });

// With tags and metadata
// Note: File visibility automatically matches bucket visibility
await hypz.files.upload({
  bucketId,
  file: Buffer.from('content'),
  fileName: 'data.json',
  tags: ['analytics', 'q4'],
  metadata: { owner: 'ops' }
});
```

### List / get / update / delete

```js
const files = await hypz.files.list(bucketId, { page: 1, limit: 50 });
const file = await hypz.files.get(fileId);
await hypz.files.update(fileId, { tags: ['public'] });
await hypz.files.delete(fileId);
```

### Download (buffer/stream/save)

```js
// Buffer
const data = await hypz.files.download(fileId);

// Stream
const stream = await hypz.files.download(fileId, { stream: true });
stream.pipe(fs.createWriteStream('out.bin'));

// Save directly
await hypz.files.download(fileId, { saveTo: 'downloads/report.pdf' });
```

---

## 5) Bulk Operations

Perform operations on multiple files at once for better efficiency.

### Bulk Delete (up to 100 files)

```js
const result = await hypz.files.bulkDelete([fileId1, fileId2, fileId3]);
logger.log(`Deleted ${result.data.deletedCount} files`);
logger.log(`Freed ${result.data.totalSize} bytes`);
```

### Bulk Update (up to 100 files)

```js
// Note: File visibility is inherited from bucket and cannot be changed
const result = await hypz.files.bulkUpdate({
  fileIds: [fileId1, fileId2, fileId3],
  tags: ['archived', '2024'],
  metadata: { processed: true }
});
logger.log(`Updated ${result.data.updatedCount} files`);
```

### Bulk Download URLs (up to 50 files)

```js
const result = await hypz.files.bulkDownload([fileId1, fileId2, fileId3]);
result.data.files.forEach(file => {
  logger.log(`${file.filename}: ${file.downloadUrl}`);
});
```

### Bulk Move (up to 100 files)

```js
const result = await hypz.files.bulkMove({
  fileIds: [fileId1, fileId2, fileId3],
  targetBucketId: 'target-bucket-id'
});
logger.log(`Moved ${result.data.movedCount} files`);
```

### Bulk Upload (up to 20 files)

```js
const fs = require('fs');

// Note: File visibility automatically matches bucket visibility
const result = await hypz.files.bulkUpload({
  bucketId: 'your-bucket-id',
  files: [
    { file: fs.createReadStream('./photo1.jpg'), filename: 'photo1.jpg' },
    { file: fs.createReadStream('./photo2.jpg'), filename: 'photo2.jpg' },
    { file: Buffer.from('content'), filename: 'data.txt' }
  ],
  tags: ['batch-upload', '2024'],
  metadata: { source: 'bulk-import' }
});

logger.log(`Uploaded ${result.data.uploadedCount} files`);
logger.log(`Total size: ${result.data.totalSize} bytes`);

// Check for partial failures
if (result.data.errors && result.data.errors.length > 0) {
  logger.log(`Errors: ${result.data.errorCount}`);
  result.data.errors.forEach(err => {
    logger.log(`  - ${err.filename}: ${err.error}`);
  });
}
```

**Note:** Bulk upload supports partial success - some files may upload successfully while others fail.

---

## 6) Private access: Signed URLs (≤ 7 days)

```js
// Generate time‑limited link
const signedUrl = await hypz.files.getSignedURL(fileId, 3600); // 1 hour
// Max allowed: 604800 seconds (7 days). Longer values are capped.
```

This hits the backend endpoint:
- POST `/files/file/:fileId/signed-url` → returns `url`
- GET `/files/file/:fileId/download-signed?token=...` → downloads without auth header

Use cases:
- Share private files with end users
- Offload downloads to the client without exposing API keys

---

## 7) Usage (metering)

```js
await hypz.usage.current();
await hypz.usage.history({ days: 30 });
await hypz.usage.analytics({ from: '2025-01-01', to: '2025-02-01' });
```

---

## 8) Error handling and retries

All API errors throw `HypzError`:

```js
try {
  await hypz.files.upload({ bucketId: 'bad-id', file: Buffer.from('x'), fileName: 'x.txt' });
} catch (err) {
  if (err.name === 'HypzError') {
    logger.error(err.statusCode, err.message, err.data);
  }
}
```

Axios retry behavior:
- Retries: `retries` (default 3)
- Delay between retries: `retryDelay` (default 1000ms)

---

## 9) Timeouts and configuration

```js
const hypz = new HypzSDK({
  apiKey: 'sk_live...',
  baseURL: 'http://localhost:5000/api/v1',
  timeout: 45000,
  retries: 5,
  retryDelay: 1500
});
```

---

## 10) TypeScript

Types are bundled. Example:

```ts
import { HypzSDK, HypzError } from '@hypz/sdk';

const hypz = new HypzSDK({ apiKey: 'sk_live...', baseURL: 'http://localhost:5000/api/v1' });
```

Key types include:
- Bucket payloads (create/update)
- File upload options (filename, tags, metadata)
- Usage responses

---

## 11) Environment examples

### Node (Express)

```js
app.post('/upload', async (req, res) => {
  const buffer = Buffer.from(req.body.content, 'base64');
  const uploaded = await hypz.files.upload({ bucketId: process.env.BUCKET_ID, file: buffer, fileName: 'in.txt' });
  res.json(uploaded);
});
```

### Serverless (AWS Lambda)

```js
exports.handler = async (event) => {
  const data = Buffer.from(event.body, 'base64');
  const uploaded = await hypz.files.upload({ bucketId: process.env.BUCKET_ID, file: data, fileName: 'lambda.bin' });
  return { statusCode: 200, body: JSON.stringify(uploaded) };
};
```

### CLI example

```js
#!/usr/bin/env node
const [,, filePath] = process.argv;
await hypz.files.upload(process.env.BUCKET_ID, filePath, { filename: path.basename(filePath) });
logger.log('Uploaded');
```

---

## 12) Troubleshooting

- “Invalid or expired API key” → Create a new key in dashboard; ensure it’s active.
- “Permission denied” → Set API key permissions (files:read/write/delete) as needed.
- “Token expired” for signed URLs → Regenerate with `getSignedURL(fileId, seconds)`.
- Large uploads time out → increase `timeout` and consider streaming.

---

## 13) FAQ

- Q: Why is there a `.tgz` file in the Node SDK folder?
- A: That’s the package tarball created by `npm pack`. It’s used for local installs and is what npm uploads when publishing. You can test locally with `npm i ./hypz-sdk-1.0.1.tgz`.

- Q: API key vs JWT?
- A: API key is best for backend/serverless. JWT is for dashboard‑authenticated flows. The SDK supports both.

- Q: Why signed URLs?
- A: They allow secure, time‑limited access to private files without exposing API keys. Max 7 days.

---

## 13) Publishing (maintainers)

```bash
# Log in once
npm login

# From hypz-sdk/nodejs
npm version patch   # or minor/major
npm publish --access public
```

---

## 14) Support

- Email: support@hypz.io
- Issues: https://github.com/ysr-hameed/hypz/issues
- Docs: https://docs.hypz.io

---

## License

MIT

- 🚀 **Simple & Intuitive API** - Easy to use methods for all operations
- 📦 **Bucket Management** - Create, list, update, and delete buckets
- 📁 **File Operations** - Upload, download, list, and delete files
- 🔑 **API Key Management** - Create and manage API keys
- 📊 **Usage Tracking** - Monitor storage and bandwidth usage
- ⚡ **Promise-based** - Modern async/await support
- 🔒 **Secure** - Built-in authentication and validation

## Documentation

### Initialize SDK

```javascript
const { HypzSDK } = require('@hypz/sdk');

const hypz = new HypzSDK({
  apiKey: 'your-api-key',
  baseURL: 'https://api.hypz.io/api/v1' // optional, defaults to production
});
```

### Bucket Operations

#### Create Bucket
```javascript
const bucket = await hypz.buckets.create({
  name: 'my-bucket',
  isPublicBucket: true
});
```

#### List Buckets
```javascript
const buckets = await hypz.buckets.list();
```

#### Get Bucket Details
```javascript
const bucket = await hypz.buckets.get(bucketId);
```

#### Update Bucket
```javascript
const updated = await hypz.buckets.update(bucketId, {
  name: 'new-name',
  isPublicBucket: false
});
```

#### Delete Bucket
```javascript
await hypz.buckets.delete(bucketId);
```

### File Operations

#### Upload File
```javascript
const file = await hypz.files.upload({
  bucketId: 'bucket-id',
  file: fileBuffer,
  fileName: 'photo.jpg'
});
```

#### List Files
```javascript
const files = await hypz.files.list(bucketId);
```

#### Get File Details
```javascript
const file = await hypz.files.get(fileId);
```

#### Download File (Authenticated)
```javascript
const fileData = await hypz.files.download(fileId);
```

#### Private Access: Signed URLs (max 7 days)
```javascript
// Generate a time-limited URL for private files (1 hour)
const signedUrl = await hypz.files.getSignedURL(fileId, 3600);
// Use with any HTTP client (browser fetch/node axios)
```

#### Delete File
```javascript
await hypz.files.delete(fileId);
```

### API Key Management

#### Create API Key
```javascript
const apiKey = await hypz.apiKeys.create({
  name: 'Production Key',
  permissions: ['files:read', 'files:write']
});
```

#### List API Keys
```javascript
const keys = await hypz.apiKeys.list();
```

#### Revoke API Key
```javascript
await hypz.apiKeys.revoke(keyId);
```

### Usage Tracking

#### Get Current Usage
```javascript
const usage = await hypz.usage.getCurrent();
logger.log('Storage used:', usage.storageUsed);
logger.log('Bandwidth used:', usage.bandwidthUsed);
```

#### Get Usage History
```javascript
const history = await hypz.usage.getHistory();
```

## Error Handling

The SDK throws `HypzError` for API errors:

```javascript
const { HypzSDK, HypzError } = require('@hypz/sdk');

try {
  const file = await hypz.files.upload({
    bucketId: 'invalid-id',
    file: buffer,
    fileName: 'test.jpg'
  });
} catch (error) {
  if (error instanceof HypzError) {
    logger.error('API Error:', error.message);
    logger.error('Status Code:', error.statusCode);
    logger.error('Response:', error.response);
  } else {
    logger.error('Unexpected error:', error);
  }
}
```

## TypeScript Support

The SDK includes TypeScript definitions. Import types:

```typescript
import { HypzSDK, HypzError, BucketOptions, FileUploadOptions } from '@hypz/sdk';
```

## Examples

### Upload Multiple Files

```javascript
const files = ['file1.jpg', 'file2.png', 'file3.pdf'];

for (const fileName of files) {
  const buffer = fs.readFileSync(fileName);
  const uploaded = await hypz.files.upload({
    bucketId: 'my-bucket-id',
    file: buffer,
    fileName
  });
  logger.log(`Uploaded: ${uploaded.url}`);
}
```

### Create Public Gallery

```javascript
// Create public bucket
const gallery = await hypz.buckets.create({
  name: 'photo-gallery',
  isPublicBucket: true
});

// Upload images (automatically public since bucket is public)
const images = ['photo1.jpg', 'photo2.jpg', 'photo3.jpg'];
const uploadedFiles = [];

for (const image of images) {
  const buffer = fs.readFileSync(image);
  const file = await hypz.files.upload({
    bucketId: gallery.id,
    file: buffer,
    fileName: image
  });
  uploadedFiles.push(file);
}

logger.log('Gallery created:', uploadedFiles.map(f => f.url));
```

### Monitor Storage Usage

```javascript
const checkUsage = async () => {
  const usage = await hypz.usage.getCurrent();
  const storageGB = (usage.storageUsed / 1024 / 1024 / 1024).toFixed(2);
  const bandwidthGB = (usage.bandwidthUsed / 1024 / 1024 / 1024).toFixed(2);
  
  logger.log(`Storage: ${storageGB} GB`);
  logger.log(`Bandwidth: ${bandwidthGB} GB`);
  
  if (usage.storageUsed > usage.storageLimit * 0.9) {
    console.warn('⚠️ Storage limit nearly reached!');
  }
};

// Check every hour
setInterval(checkUsage, 60 * 60 * 1000);
```

## Support

- 📧 Email: support@hypz.io
- 💬 Discord: [Join our community](https://discord.gg/hypz)
- 📚 Docs: [https://docs.hypz.io](https://docs.hypz.io)
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/hypz-sdk/issues)

## License

MIT License - see LICENSE file for details

## Contributing

Contributions are welcome! Please read our contributing guidelines first.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
