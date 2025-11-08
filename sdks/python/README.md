# Hypz SDK for Python

Official Python client library for [Hypz Cloud Storage](https://hypz.io) - a powerful S3-compatible file storage platform with advanced features.

[![Python Version](https://img.shields.io/badge/python-3.8+-blue.svg)](https://www.python.org/downloads/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## Features

- 🚀 **Simple & Intuitive** - Clean Pythonic API for all operations
- 📦 **Bucket Management** - Create, list, update, and delete buckets
- 📁 **File Operations** - Upload, download, list, and delete files
- 🔒 **Private Access** - Generate time-limited signed URLs (max 7 days)
- 🔑 **Flexible Auth** - Support for API keys and JWT tokens
- ⚡ **Async Support** - Non-blocking operations for better performance
- 📊 **Usage Tracking** - Monitor storage and bandwidth consumption
- 🛡️ **Error Handling** - Comprehensive exception handling

## Installation

```bash
pip install hypz-sdk
```

## Quick Start

```python
from hypz_sdk import Hypz

# Initialize client
client = Hypz(
    api_key="sk_live_your_key",
    base_url="http://localhost:5000/api/v1"
)

# Create a private bucket
bucket = client.buckets.create(
    name="my-documents",
    visibility="private"
)

# Upload a file
file = client.files.upload(
    bucket_id=bucket["id"],
    data=b"Hello, Hypz!",
    filename="hello.txt"
)

# Generate a signed URL (1 hour)
signed_url = client.files.get_signed_url(
    file_id=file["id"],
    expires_in=3600
)

print(f"Download link: {signed_url}")
```

## Documentation

### Authentication

The SDK supports two authentication methods:

```python
# API Key (recommended for server-side)
from hypz_sdk import Hypz

client = Hypz(
    api_key="sk_live_your_key",
    base_url="http://localhost:5000/api/v1"
)

# JWT Token (for dashboard-authenticated requests)
client = Hypz(
    jwt="your_jwt_token",
    base_url="http://localhost:5000/api/v1"
)
```

### Bucket Operations

#### Create Bucket

```python
bucket = client.buckets.create(
    name="my-bucket",
    visibility="private",  # or "public"
    description="My private bucket"
)
```

#### List Buckets

```python
buckets = client.buckets.list(page=1, limit=20)
for bucket in buckets:
    print(f"{bucket['name']}: {bucket['visibility']}")
```

#### Get Bucket

```python
bucket = client.buckets.get(bucket_id)
print(f"Bucket: {bucket['name']}")
```

#### Update Bucket

```python
updated = client.buckets.update(
    bucket_id,
    name="renamed-bucket",
    visibility="public"
)
```

#### Delete Bucket

```python
client.buckets.delete(bucket_id)
```

### File Operations

#### Upload File (Bytes)

```python
file = client.files.upload(
    bucket_id=bucket["id"],
    data=b"File content here",
    filename="document.txt"
)
```

#### Upload File (From Path)

```python
file = client.files.upload_path(
    bucket_id=bucket["id"],
    file_path="/path/to/document.pdf",
    filename="document.pdf"
)
```

#### Upload with Metadata and Tags

```python
file = client.files.upload(
    bucket_id=bucket["id"],
    data=b"Content",
    filename="data.json",
    tags=["analytics", "2025"],
    metadata={"source": "api", "version": "1.0"}
)
```

#### List Files

```python
files = client.files.list(bucket_id, page=1, limit=50)
for file in files:
    print(f"{file['filename']}: {file['size']} bytes")
```

#### Get File Details

```python
file = client.files.get(file_id)
print(f"File: {file['filename']}")
print(f"Size: {file['size']} bytes")
print(f"Public: {file['isPublic']}")  # Reflects bucket visibility
```

#### Download File

```python
# Download as bytes
data = client.files.download(file_id)
with open("downloaded.pdf", "wb") as f:
    f.write(data)
```

#### Download to File

```python
client.files.download_to(file_id, dest_path="downloads/document.pdf")
```

#### Update File

```python
# Note: File visibility is inherited from bucket and cannot be changed
updated = client.files.update(
    file_id,
    tags=["public", "updated"]
)
```

#### Delete File

```python
client.files.delete(file_id)
```

### Private Access: Signed URLs

Generate time-limited URLs for secure access to private files:

```python
# Generate signed URL (1 hour)
signed_url = client.files.get_signed_url(
    file_id=file["id"],
    expires_in=3600  # seconds, max 604800 (7 days)
)

# Use the signed URL
import requests
response = requests.get(signed_url)
with open("downloaded.pdf", "wb") as f:
    f.write(response.content)
```

**Note:** Maximum expiry time is 7 days (604800 seconds). Values exceeding this will be capped.

### Usage & Analytics

```python
# Get current usage
usage = client.usage.current()
print(f"Storage: {usage['storageUsed']} bytes")
print(f"Bandwidth: {usage['bandwidthUsed']} bytes")

# Get usage history
history = client.usage.history(days=30)

# Get detailed analytics
analytics = client.usage.analytics(
    start="2025-01-01",
    end="2025-01-31"
)
```

## Error Handling

The SDK raises `HypzError` for API errors:

```python
from hypz_sdk import Hypz, HypzError

try:
    file = client.files.get("invalid-id")
except HypzError as e:
    print(f"API Error: {e.status_code}")
    print(f"Message: {e.message}")
    print(f"Data: {e.data}")
except Exception as e:
    print(f"Unexpected error: {e}")
```

## Configuration

### Timeouts and Retries

```python
client = Hypz(
    api_key="sk_live_your_key",
    base_url="http://localhost:5000/api/v1",
    timeout=45,        # seconds (default: 30)
    retries=5,         # retry attempts (default: 3)
    retry_delay=2.0    # seconds (default: 1.0)
)
```

### Custom Headers

```python
client = Hypz(
    api_key="sk_live_your_key",
    base_url="http://localhost:5000/api/v1",
    headers={
        "X-Custom-Header": "value"
    }
)
```

## Advanced Usage

### Streaming Large Files

```python
# Stream upload
with open("large-file.zip", "rb") as f:
    file = client.files.upload_stream(
        bucket_id=bucket["id"],
        stream=f,
        filename="large-file.zip"
    )

# Stream download
with client.files.download_stream(file_id) as stream:
    with open("output.zip", "wb") as f:
        for chunk in stream.iter_content(chunk_size=8192):
            f.write(chunk)
```

### Batch Operations

```python
# Upload multiple files
files_to_upload = [
    ("file1.txt", b"Content 1"),
    ("file2.txt", b"Content 2"),
    ("file3.txt", b"Content 3")
]

uploaded_files = []
for filename, data in files_to_upload:
    file = client.files.upload(
        bucket_id=bucket["id"],
        data=data,
        filename=filename
    )
    uploaded_files.append(file)

# Delete multiple files
for file in uploaded_files:
    client.files.delete(file["id"])
```

## Best Practices

### Security

- ✅ **Never expose API keys** in client-side code
- ✅ **Use signed URLs** for temporary access to private files
- ✅ **Rotate API keys** regularly
- ✅ **Use environment variables** for credentials
- ✅ **Set minimum necessary permissions** on API keys

### Performance

- ✅ **Use streaming** for large files (>10MB)
- ✅ **Implement retries** with exponential backoff
- ✅ **Cache bucket IDs** to reduce API calls
- ✅ **Use pagination** for large file lists
- ✅ **Enable compression** for text-based files

### Error Handling

- ✅ **Always wrap API calls** in try-except blocks
- ✅ **Log errors** for debugging
- ✅ **Implement fallback strategies** for critical operations
- ✅ **Validate inputs** before API calls
- ✅ **Monitor API rate limits**

## Examples

### Complete Upload/Download Flow

```python
from hypz_sdk import Hypz
import os

# Initialize
client = Hypz(
    api_key=os.environ["HYPZ_API_KEY"],
    base_url="http://localhost:5000/api/v1"
)

# Create bucket
bucket = client.buckets.create(
    name="demo-bucket",
    visibility="private"
)

# Upload file
with open("document.pdf", "rb") as f:
    file = client.files.upload(
        bucket_id=bucket["id"],
        data=f.read(),
        filename="document.pdf"
    )

# Generate signed URL (1 hour)
url = client.files.get_signed_url(file["id"], expires_in=3600)
print(f"Share this URL: {url}")

# Download via signed URL
import requests
response = requests.get(url)
with open("downloaded.pdf", "wb") as f:
    f.write(response.content)

# Cleanup
client.files.delete(file["id"])
client.buckets.delete(bucket["id"])
```

### Flask Integration

```python
from flask import Flask, request, jsonify
from hypz_sdk import Hypz

app = Flask(__name__)
client = Hypz(
    api_key=os.environ["HYPZ_API_KEY"],
    base_url="http://localhost:5000/api/v1"
)

@app.route('/upload', methods=['POST'])
def upload():
    file = request.files['file']
    
    uploaded = client.files.upload(
        bucket_id=os.environ["BUCKET_ID"],
        data=file.read(),
        filename=file.filename
    )
    
    return jsonify({
        "success": True,
        "file_id": uploaded["id"],
        "url": uploaded["url"]
    })

@app.route('/download/<file_id>')
def download(file_id):
    # Generate signed URL
    url = client.files.get_signed_url(file_id, expires_in=300)
    return jsonify({"url": url})
```

## Troubleshooting

### Common Issues

**Problem:** `HypzError: 401 Unauthorized`
- **Solution:** Check that your API key is valid and active

**Problem:** `HypzError: 404 Not Found`
- **Solution:** Verify the bucket ID or file ID exists

**Problem:** `TimeoutError`
- **Solution:** Increase timeout or check network connectivity

**Problem:** Signed URL returns `403 Forbidden`
- **Solution:** URL may be expired, generate a new one

**Problem:** Large file upload fails
- **Solution:** Use streaming upload or increase timeout

## Requirements

- Python 3.8+
- requests >= 2.28.0
- urllib3 >= 1.26.0

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- 📧 **Email:** support@hypz.io
- 🐛 **Issues:** [GitHub Issues](https://github.com/ysr-hameed/hypz/issues)
- 📚 **Documentation:** [https://docs.hypz.io](https://docs.hypz.io)
- 💬 **Community:** [Discord](https://discord.gg/hypz)

## Links

- [Official Website](https://hypz.io)
- [API Documentation](https://docs.hypz.io/api)
- [Node.js SDK](https://www.npmjs.com/package/@hypz/sdk)
- [Java SDK](https://github.com/ysr-hameed/hypz/tree/main/hypz-sdk/java)

---

Made with ❤️ by the Hypz Team
