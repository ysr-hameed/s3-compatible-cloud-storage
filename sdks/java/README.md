# Hypz SDK for Java

Official Java client library for [Hypz Cloud Storage](https://hypz.io) - a powerful S3-compatible file storage platform with advanced features.

[![Java Version](https://img.shields.io/badge/java-11+-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## Features

- 🚀 **Modern Java** - Built with Java 11+ features
- 📦 **Bucket Management** - Create, list, update, and delete buckets
- 📁 **File Operations** - Upload, download, list, and delete files
- 🔒 **Private Access** - Generate time-limited signed URLs (max 7 days)
- 🔑 **Flexible Auth** - Support for API keys and JWT tokens
- ⚡ **High Performance** - Optimized for concurrent operations
- 📊 **Usage Tracking** - Monitor storage and bandwidth consumption
- 🛡️ **Error Handling** - Comprehensive exception handling
- 🔧 **Thread Safe** - Safe for use in multi-threaded applications

## Installation

### Gradle (via JitPack)

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ysr-hameed.hypz:java:1.0.1'
}
```

### Maven (via JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ysr-hameed.hypz</groupId>
    <artifactId>java</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Manual JAR Download

Download the latest JAR from [GitHub Releases](https://github.com/ysr-hameed/hypz/releases).

## Quick Start

```java
import io.hypz.HypzClient;
import io.hypz.models.*;

public class Example {
    public static void main(String[] args) {
        // Initialize client
        HypzClient client = new HypzClient.Builder()
            .apiKey("sk_live_your_key")
            .baseUrl("http://localhost:5000/api/v1")
            .build();

        // Create a private bucket
        Bucket bucket = client.buckets().create(
            "my-documents",
            "private",
            "My private bucket"
        );

        // Upload a file
        String content = "Hello, Hypz!";
        HypzFile file = client.files().upload(
            bucket.getId(),
            content.getBytes(StandardCharsets.UTF_8),
            "hello.txt"
        );

        // Generate a signed URL (1 hour)
        String signedUrl = client.files().getSignedUrl(
            file.getId(),
            3600  // seconds
        );

        System.out.println("Download link: " + signedUrl);
    }
}
```

## Documentation

### Authentication

The SDK supports two authentication methods:

```java
import io.hypz.HypzClient;

// API Key (recommended for server-side)
HypzClient client = new HypzClient.Builder()
    .apiKey("sk_live_your_key")
    .baseUrl("http://localhost:5000/api/v1")
    .build();

// JWT Token (for dashboard-authenticated requests)
HypzClient client = new HypzClient.Builder()
    .jwt("your_jwt_token")
    .baseUrl("http://localhost:5000/api/v1")
    .build();
```

### Bucket Operations

#### Create Bucket

```java
Bucket bucket = client.buckets().create(
    "my-bucket",
    "private",  // or "public"
    "My private bucket"
);
```

#### List Buckets

```java
List<Bucket> buckets = client.buckets().list(1, 20);
for (Bucket bucket : buckets) {
    System.out.println(bucket.getName() + ": " + bucket.getVisibility());
}
```

#### Get Bucket

```java
Bucket bucket = client.buckets().get(bucketId);
System.out.println("Bucket: " + bucket.getName());
```

#### Update Bucket

```java
Bucket updated = client.buckets().update(
    bucketId,
    "renamed-bucket",
    "public"
);
```

#### Delete Bucket

```java
client.buckets().delete(bucketId);
```

### File Operations

#### Upload File (Bytes)

```java
import java.nio.charset.StandardCharsets;

byte[] content = "File content here".getBytes(StandardCharsets.UTF_8);
HypzFile file = client.files().upload(
    bucket.getId(),
    content,
    "document.txt"
);
```

#### Upload File (From Path)

```java
import java.nio.file.Files;
import java.nio.file.Path;

Path filePath = Path.of("/path/to/document.pdf");
byte[] data = Files.readAllBytes(filePath);

HypzFile file = client.files().upload(
    bucket.getId(),
    data,
    "document.pdf"
);
```

#### Upload with Metadata and Tags

```java
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

List<String> tags = Arrays.asList("analytics", "2025");
Map<String, String> metadata = new HashMap<>();
metadata.put("source", "api");
metadata.put("version", "1.0");

HypzFile file = client.files().upload(
    bucket.getId(),
    data,
    "data.json",
    tags,
    metadata
);
```

#### List Files

```java
List<HypzFile> files = client.files().list(bucketId, 1, 50);
for (HypzFile file : files) {
    System.out.println(file.getFilename() + ": " + file.getSize() + " bytes");
}
```

#### Get File Details

```java
HypzFile file = client.files().get(fileId);
System.out.println("File: " + file.getFilename());
System.out.println("Size: " + file.getSize() + " bytes");
System.out.println("Public: " + file.isPublic());  // Reflects bucket visibility
```

#### Download File

```java
import java.nio.file.Files;
import java.nio.file.Path;

// Download as bytes
byte[] data = client.files().download(fileId);

// Save to file
Path outputPath = Path.of("downloaded.pdf");
Files.write(outputPath, data);
```

#### Download to File (Direct)

```java
client.files().downloadTo(fileId, "downloads/document.pdf");
```

#### Update File

```java
import java.util.Arrays;

// Note: File visibility is inherited from bucket and cannot be changed
HypzFile updated = client.files().update(
    fileId,
    Arrays.asList("public", "updated")  // tags only
);
```

#### Delete File

```java
client.files().delete(fileId);
```

### Private Access: Signed URLs

Generate time-limited URLs for secure access to private files:

```java
// Generate signed URL (1 hour)
String signedUrl = client.files().getSignedUrl(
    file.getId(),
    3600  // seconds, max 604800 (7 days)
);

// Download using signed URL
URL url = new URL(signedUrl);
try (InputStream in = url.openStream()) {
    Files.copy(in, Path.of("downloaded.pdf"));
}
```

**Note:** Maximum expiry time is 7 days (604800 seconds). Values exceeding this will be capped.

### Usage & Analytics

```java
// Get current usage
Usage usage = client.usage().current();
System.out.println("Storage: " + usage.getStorageUsed() + " bytes");
System.out.println("Bandwidth: " + usage.getBandwidthUsed() + " bytes");

// Get usage history
List<UsageHistory> history = client.usage().history(30);

// Get detailed analytics
Analytics analytics = client.usage().analytics(
    "2025-01-01",
    "2025-01-31"
);
```

## Error Handling

The SDK throws `HypzException` for API errors:

```java
import io.hypz.exceptions.HypzException;

try {
    HypzFile file = client.files().get("invalid-id");
} catch (HypzException e) {
    System.err.println("API Error: " + e.getStatusCode());
    System.err.println("Message: " + e.getMessage());
    System.err.println("Data: " + e.getData());
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

## Configuration

### Timeouts and Retries

```java
HypzClient client = new HypzClient.Builder()
    .apiKey("sk_live_your_key")
    .baseUrl("http://localhost:5000/api/v1")
    .timeout(45)        // seconds (default: 30)
    .retries(5)         // retry attempts (default: 3)
    .retryDelay(2000)   // milliseconds (default: 1000)
    .build();
```

### Custom Headers

```java
import java.util.HashMap;
import java.util.Map;

Map<String, String> headers = new HashMap<>();
headers.put("X-Custom-Header", "value");

HypzClient client = new HypzClient.Builder()
    .apiKey("sk_live_your_key")
    .baseUrl("http://localhost:5000/api/v1")
    .headers(headers)
    .build();
```

### Connection Pooling

```java
HypzClient client = new HypzClient.Builder()
    .apiKey("sk_live_your_key")
    .baseUrl("http://localhost:5000/api/v1")
    .maxConnections(100)        // default: 50
    .keepAliveSeconds(300)      // default: 60
    .build();
```

## Advanced Usage

### Streaming Large Files

```java
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

// Stream upload
Path inputPath = Path.of("large-file.zip");
try (InputStream in = Files.newInputStream(inputPath)) {
    HypzFile file = client.files().uploadStream(
        bucket.getId(),
        in,
        "large-file.zip"
    );
}

// Stream download
Path outputPath = Path.of("output.zip");
try (OutputStream out = Files.newOutputStream(outputPath)) {
    client.files().downloadStream(fileId, out);
}
```

### Batch Operations with Parallel Streams

```java
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// Upload multiple files concurrently
List<String> filePaths = Arrays.asList(
    "file1.txt",
    "file2.txt",
    "file3.txt"
);

List<CompletableFuture<HypzFile>> futures = filePaths.stream()
    .map(path -> CompletableFuture.supplyAsync(() -> {
        try {
            byte[] data = Files.readAllBytes(Path.of(path));
            return client.files().upload(
                bucket.getId(),
                data,
                Path.of(path).getFileName().toString()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }))
    .collect(Collectors.toList());

// Wait for all uploads
List<HypzFile> uploadedFiles = futures.stream()
    .map(CompletableFuture::join)
    .collect(Collectors.toList());
```

### Async Operations

```java
import java.util.concurrent.CompletableFuture;

// Async upload
CompletableFuture<HypzFile> future = CompletableFuture.supplyAsync(() ->
    client.files().upload(bucket.getId(), data, "async-file.txt")
);

// Do other work...

// Wait for completion
HypzFile file = future.get();
```

## Best Practices

### Security

- ✅ **Never expose API keys** in client-side code or version control
- ✅ **Use signed URLs** for temporary access to private files
- ✅ **Rotate API keys** regularly
- ✅ **Use environment variables** for credentials
- ✅ **Set minimum necessary permissions** on API keys
- ✅ **Enable SSL/TLS** in production

### Performance

- ✅ **Use streaming** for large files (>10MB)
- ✅ **Implement retry logic** with exponential backoff
- ✅ **Cache bucket IDs** to reduce API calls
- ✅ **Use connection pooling** for concurrent operations
- ✅ **Compress files** before upload when appropriate
- ✅ **Use parallel streams** for batch operations

### Error Handling

- ✅ **Always catch HypzException** in production code
- ✅ **Log errors** with appropriate context
- ✅ **Implement fallback strategies** for critical operations
- ✅ **Validate inputs** before API calls
- ✅ **Monitor API rate limits**
- ✅ **Use timeouts** to prevent hanging requests

### Resource Management

- ✅ **Close streams** properly (use try-with-resources)
- ✅ **Reuse HypzClient** instances
- ✅ **Shutdown client** when done: `client.shutdown()`
- ✅ **Use appropriate buffer sizes** for streaming

## Examples

### Complete Upload/Download Flow

```java
import io.hypz.HypzClient;
import io.hypz.models.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadDownloadExample {
    public static void main(String[] args) {
        // Initialize
        HypzClient client = new HypzClient.Builder()
            .apiKey(System.getenv("HYPZ_API_KEY"))
            .baseUrl("http://localhost:5000/api/v1")
            .build();

        try {
            // Create bucket
            Bucket bucket = client.buckets().create(
                "demo-bucket",
                "private",
                "Demo bucket"
            );

            // Upload file
            Path filePath = Path.of("document.pdf");
            byte[] data = Files.readAllBytes(filePath);
            
            HypzFile file = client.files().upload(
                bucket.getId(),
                data,
                "document.pdf"
            );

            // Generate signed URL (1 hour)
            String url = client.files().getSignedUrl(
                file.getId(),
                3600
            );
            System.out.println("Share this URL: " + url);

            // Download via signed URL
            Path downloadPath = Path.of("downloaded.pdf");
            Files.copy(
                new URL(url).openStream(),
                downloadPath
            );

            // Cleanup
            client.files().delete(file.getId());
            client.buckets().delete(bucket.getId());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.shutdown();
        }
    }
}
```

### Spring Boot Integration

```java
import io.hypz.HypzClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class HypzConfig {
    @Bean
    public HypzClient hypzClient(
        @Value("${hypz.api-key}") String apiKey,
        @Value("${hypz.base-url}") String baseUrl
    ) {
        return new HypzClient.Builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .build();
    }
}

@Service
public class FileService {
    private final HypzClient hypzClient;
    private final String bucketId;

    public FileService(
        HypzClient hypzClient,
        @Value("${hypz.bucket-id}") String bucketId
    ) {
        this.hypzClient = hypzClient;
        this.bucketId = bucketId;
    }

    public HypzFile uploadFile(MultipartFile file) throws IOException {
        return hypzClient.files().upload(
            bucketId,
            file.getBytes(),
            file.getOriginalFilename()
        );
    }

    public String getDownloadUrl(String fileId) {
        return hypzClient.files().getSignedUrl(fileId, 300);
    }
}
```

### Servlet Integration

```java
import io.hypz.HypzClient;
import io.hypz.models.HypzFile;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private HypzClient client;

    @Override
    public void init() {
        client = new HypzClient.Builder()
            .apiKey(System.getenv("HYPZ_API_KEY"))
            .baseUrl("http://localhost:5000/api/v1")
            .build();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Part filePart = req.getPart("file");
        
        HypzFile file = client.files().upload(
            System.getenv("BUCKET_ID"),
            filePart.getInputStream().readAllBytes(),
            filePart.getSubmittedFileName()
        );

        resp.setContentType("application/json");
        resp.getWriter().write(String.format(
            "{\"success\":true,\"fileId\":\"%s\",\"url\":\"%s\"}",
            file.getId(),
            file.getUrl()
        ));
    }

    @Override
    public void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }
}
```

## Troubleshooting

### Common Issues

**Problem:** `HypzException: 401 Unauthorized`
- **Solution:** Check that your API key is valid and active
- Verify the API key is correctly set in environment variables

**Problem:** `HypzException: 404 Not Found`
- **Solution:** Verify the bucket ID or file ID exists
- Check for typos in IDs

**Problem:** `SocketTimeoutException`
- **Solution:** Increase timeout or check network connectivity
- Use streaming for large files

**Problem:** Signed URL returns `403 Forbidden`
- **Solution:** URL may be expired, generate a new one
- Check system clock is synchronized

**Problem:** Large file upload fails
- **Solution:** Use streaming upload method
- Increase timeout and buffer size

**Problem:** `OutOfMemoryError` when uploading large files
- **Solution:** Use `uploadStream()` instead of loading entire file into memory
- Increase JVM heap size if necessary

## Requirements

- Java 11 or higher
- Gradle 6.0+ or Maven 3.6+
- Dependencies:
  - OkHttp 4.x
  - Jackson 2.x
  - SLF4J 1.7+

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
- 📖 **Javadoc:** [https://javadoc.io/doc/io.hypz/hypz-sdk](https://javadoc.io/doc/io.hypz/hypz-sdk)

## Links

- [Official Website](https://hypz.io)
- [API Documentation](https://docs.hypz.io/api)
- [Node.js SDK](https://www.npmjs.com/package/@hypz/sdk)
- [Python SDK](https://pypi.org/project/hypz-sdk/)
- [Maven Repository](https://mvnrepository.com/artifact/io.hypz/hypz-sdk)

---

Made with ❤️ by the Hypz Team
