# 🚀 Hypz Cloud Storage Platform

<div align="center">

![Hypz Logo](https://img.shields.io/badge/Hypz-Cloud%20Storage-blue?style=for-the-badge)

**S3-Compatible Cloud Storage Platform with Modern SDKs**

[![Node.js SDK](https://img.shields.io/npm/v/@hypz/sdk?label=Node.js&style=flat-square)](https://www.npmjs.com/package/@hypz/sdk)
[![Python SDK](https://img.shields.io/pypi/v/hypz-sdk?label=Python&style=flat-square)](https://pypi.org/project/hypz-sdk/)
[![Java SDK](https://img.shields.io/badge/Java-1.0.1-orange?style=flat-square)](https://jitpack.io/#ysr-hameed/hypz-cloud)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)

[Website](https://hypz.io) • [Documentation](https://hypz.io/docs) • [API Reference](https://hypz.io/api)

</div>

---

## 📖 Overview

Hypz is a **production-ready cloud storage platform** that provides S3-compatible APIs for file storage and management. Built with modern technologies and designed for scalability, it offers developers a simple yet powerful solution for managing files, buckets, and storage operations.

### ✨ Key Features

- 🗂️ **Bucket Management** - Create, organize, and manage storage buckets with public/private access
- 📤 **File Operations** - Upload, download, move, and delete files with ease
- 🔐 **Flexible Authentication** - Support for both API keys and JWT tokens
- ⚡ **Bulk Operations** - Efficient batch operations for multiple files
- 📊 **Usage Analytics** - Track storage consumption and bandwidth usage
- 🔒 **Access Control** - Granular permissions and private bucket support
- 🌐 **Multiple SDKs** - Official SDKs for Node.js, Python, and Java
- 💰 **Flexible Pricing** - Free tier, pro plans, and pay-as-you-go options

---

## 🛠️ Tech Stack

### Backend
- **Runtime**: Node.js with Express.js
- **Database**: PostgreSQL with UUID-based architecture
- **Storage**: Backblaze B2 (S3-compatible)
- **Authentication**: JWT + API Key based auth
- **Payment**: Stripe integration for subscriptions

### Frontend
- **Framework**: React + Vite
- **Styling**: Tailwind CSS
- **State Management**: Context API
- **Routing**: React Router

### SDKs
- **Node.js**: Axios-based HTTP client
- **Python**: Requests-based HTTP client
- **Java**: Pure Java implementation with Gson

---

## 📦 SDK Installation

### Node.js / JavaScript

```bash
npm install @hypz/sdk
```

```javascript
const Hypz = require('@hypz/sdk');

const client = new Hypz('your-api-key');

// Create bucket
const bucket = await client.createBucket('my-bucket', false);

// Upload file
const upload = await client.uploadFile(bucket.id, './document.pdf', 'docs/document.pdf');
console.log('File URL:', upload.url);
```

**NPM Package**: [@hypz/sdk](https://www.npmjs.com/package/@hypz/sdk)

---

### Python

```bash
pip install hypz-sdk
```

```python
from hypz import Hypz

client = Hypz('your-api-key')

# Create bucket
bucket = client.create_bucket('my-bucket', is_public=False)

# Upload file
upload = client.upload_file(bucket['id'], 'document.pdf', 'docs/document.pdf')
print('File URL:', upload['url'])
```

**PyPI Package**: [hypz-sdk](https://pypi.org/project/hypz-sdk/)

---

### Java / Android

**Gradle:**
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ysr-hameed:hypz-cloud:1.0.1'
}
```

**Maven:**
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.ysr-hameed</groupId>
    <artifactId>hypz-cloud</artifactId>
    <version>1.0.1</version>
</dependency>
```

```java
import com.hypz.sdk.HypzClient;

HypzClient client = new HypzClient("your-api-key");

// Create bucket
Bucket bucket = client.createBucket("my-bucket", false);

// Upload file
FileUploadResponse upload = client.uploadFile(bucket.id, new File("document.pdf"), "docs/document.pdf");
System.out.println("File URL: " + upload.url);
```

**JitPack**: [hypz-cloud](https://jitpack.io/#ysr-hameed/hypz-cloud)

---

## 🚀 Quick Start

### 1. Get Your API Key

Sign up at [hypz.io](https://hypz.io) and generate an API key from your dashboard.

### 2. Install SDK

Choose your preferred language and install the SDK (see installation instructions above).

### 3. Start Coding

```javascript
// Node.js Example
const Hypz = require('@hypz/sdk');
const client = new Hypz('your-api-key');

async function main() {
  // Create a bucket
  const bucket = await client.createBucket('my-app-files', false);
  
  // Upload a file
  const upload = await client.uploadFile(bucket.id, './image.jpg', 'images/photo.jpg');
  
  // List files
  const files = await client.listFiles(bucket.id, 1, 20);
  console.log(`Total files: ${files.total}`);
  
  // Get usage stats
  const usage = await client.getUsage();
  console.log(`Storage used: ${usage.totalStorage} bytes`);
}

main();
```

---

## 📚 API Features

### Bucket Operations
- ✅ Create buckets (public/private)
- ✅ List all buckets with pagination
- ✅ Update bucket settings
- ✅ Delete buckets
- ✅ Get bucket details

### File Operations
- ✅ Upload files (single/multipart)
- ✅ Download files
- ✅ List files with filtering
- ✅ Move files between buckets
- ✅ Delete files
- ✅ Get file metadata

### Bulk Operations
- ✅ Bulk delete multiple files
- ✅ Bulk move files
- ✅ Bulk download (ZIP archive)

### Usage & Analytics
- ✅ Track storage consumption
- ✅ Monitor bandwidth usage
- ✅ View file counts
- ✅ Per-bucket statistics

---

## 🏗️ Architecture

```
┌─────────────────┐
│   React App     │  Frontend (Vite + React + Tailwind)
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  Express API    │  Backend (Node.js + Express)
└────────┬────────┘
         │
    ┌────┴────┬───────────┬──────────┐
    ↓         ↓           ↓          ↓
┌────────┐ ┌──────┐  ┌────────┐ ┌────────┐
│  Auth  │ │  DB  │  │Storage │ │Payments│
│  JWT   │ │ PG   │  │  B2    │ │ Stripe │
└────────┘ └──────┘  └────────┘ └────────┘
```

---

## 🔐 Authentication

Hypz supports two authentication methods:

### 1. API Keys (Recommended for SDKs)
```javascript
const client = new Hypz('sk_live_your_api_key');
```

### 2. JWT Tokens (For dashboard/web)
```javascript
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 💰 Pricing Plans

| Plan | Storage | Bandwidth | Files | Price |
|------|---------|-----------|-------|-------|
| **Free Forever** | 5 GB | 10 GB/month | Unlimited | $0 |
| **Pro Monthly** | 100 GB | 200 GB/month | Unlimited | $9.99/mo |
| **Pay as You Go** | $0.02/GB | $0.01/GB | Unlimited | Pay per use |

---

## 📖 Documentation

Comprehensive documentation is available in the `/docs` directory:

- [Getting Started](./docs/getting-started.md)
- [API Reference](./docs/api-reference.md)
- [SDK Guides](./docs/sdk-guides/)
  - [Node.js SDK](./docs/sdk-guides/nodejs.md)
  - [Python SDK](./docs/sdk-guides/python.md)
  - [Java SDK](./docs/sdk-guides/java.md)
- [Authentication](./docs/authentication.md)
- [Error Handling](./docs/error-handling.md)
- [Best Practices](./docs/best-practices.md)

---

## 🤝 Contributing

This is a public showcase repository for the Hypz Cloud Storage Platform. The main private repository contains the full backend and frontend code.

For SDK contributions or issues:
- **Node.js SDK**: Report issues on npm
- **Python SDK**: Report issues on PyPI
- **Java SDK**: Create issues in this repository

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🔗 Links

- **Website**: [https://hypz.io](https://hypz.io)
- **Documentation**: [https://hypz.io/docs](https://hypz.io/docs)
- **Node.js SDK**: [@hypz/sdk on npm](https://www.npmjs.com/package/@hypz/sdk)
- **Python SDK**: [hypz-sdk on PyPI](https://pypi.org/project/hypz-sdk/)
- **Java SDK**: [hypz-cloud on JitPack](https://jitpack.io/#ysr-hameed/hypz-cloud)

---

## 👨‍💻 Author

**Your Name**
- GitHub: [@ysr-hameed](https://github.com/ysr-hameed)
- Email: support@hypz.io

---

<div align="center">

**⭐ Star this repository if you find it useful!**

Made with ❤️ for developers who need simple, powerful cloud storage

</div>
