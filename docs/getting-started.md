# Getting Started with Hypz Cloud Storage

Welcome to Hypz! This guide will help you get started with our cloud storage platform in just a few minutes.

## Step 1: Create an Account

1. Visit [hypz.io](https://hypz.io)
2. Click "Sign Up" and create your account
3. Verify your email address

## Step 2: Generate an API Key

1. Log in to your dashboard
2. Navigate to **Settings** → **API Keys**
3. Click "Generate New API Key"
4. Copy and save your API key securely (you won't see it again!)

## Step 3: Choose Your SDK

Install the SDK for your preferred programming language:

### Node.js
```bash
npm install @hypz/sdk
```

### Python
```bash
pip install hypz-sdk
```

### Java
Add to your `build.gradle`:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.ysr-hameed:hypz-cloud:1.0.1'
}
```

## Step 4: Write Your First Code

### Node.js Example
```javascript
const Hypz = require('@hypz/sdk');

const client = new Hypz('your-api-key-here');

async function quickStart() {
  try {
    // Create a bucket
    const bucket = await client.createBucket('my-first-bucket', false);
    console.log('✅ Bucket created:', bucket.id);
    
    // Upload a file
    const upload = await client.uploadFile(
      bucket.id,
      './test-file.txt',
      'uploads/test.txt'
    );
    console.log('✅ File uploaded:', upload.url);
    
    // List files
    const files = await client.listFiles(bucket.id);
    console.log('📁 Files in bucket:', files.files.length);
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  }
}

quickStart();
```

### Python Example
```python
from hypz import Hypz

client = Hypz('your-api-key-here')

# Create a bucket
bucket = client.create_bucket('my-first-bucket', is_public=False)
print(f'✅ Bucket created: {bucket["id"]}')

# Upload a file
upload = client.upload_file(bucket['id'], 'test-file.txt', 'uploads/test.txt')
print(f'✅ File uploaded: {upload["url"]}')

# List files
files = client.list_files(bucket['id'])
print(f'📁 Files in bucket: {len(files["files"])}')
```

### Java Example
```java
import com.hypz.sdk.HypzClient;
import com.hypz.sdk.models.*;
import java.io.File;

public class QuickStart {
    public static void main(String[] args) {
        HypzClient client = new HypzClient("your-api-key-here");
        
        try {
            // Create a bucket
            Bucket bucket = client.createBucket("my-first-bucket", false);
            System.out.println("✅ Bucket created: " + bucket.id);
            
            // Upload a file
            FileUploadResponse upload = client.uploadFile(
                bucket.id,
                new File("test-file.txt"),
                "uploads/test.txt"
            );
            System.out.println("✅ File uploaded: " + upload.url);
            
            // List files
            FileListResponse files = client.listFiles(bucket.id, 1, 20);
            System.out.println("📁 Files in bucket: " + files.total);
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
```

## Step 5: Explore More Features

Now that you have the basics working, explore more features:

- **Bucket Management**: Create public/private buckets
- **File Operations**: Upload, download, move, delete files
- **Bulk Operations**: Batch operations for efficiency
- **Usage Analytics**: Track your storage and bandwidth
- **Access Control**: Manage permissions and visibility

## Next Steps

- Read the [API Reference](./api-reference.md)
- Check out [SDK-specific guides](./sdk-guides/)
- Learn about [Authentication](./authentication.md)
- Review [Best Practices](./best-practices.md)

## Getting Help

- **Documentation**: [hypz.io/docs](https://hypz.io/docs)
- **Email**: support@hypz.io
- **GitHub Issues**: [github.com/ysr-hameed/hypz-cloud/issues](https://github.com/ysr-hameed/hypz-cloud/issues)

Happy coding! 🚀
