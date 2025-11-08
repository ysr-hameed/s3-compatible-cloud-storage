"""
Basic usage example for Hypz Python SDK
"""

from hypz import HypzClient, HypzError
import os

# Get API key from environment or use directly
API_KEY = os.getenv('HYPZ_API_KEY', 'your_api_key_here')
BASE_URL = os.getenv('HYPZ_API_URL', 'http://localhost:5000/api/v1')

def main():
    # Initialize client
    print("Initializing Hypz client...")
    client = HypzClient(api_key=API_KEY, base_url=BASE_URL)
    
    try:
        # Create a bucket
        print("\n1. Creating a bucket...")
        bucket = client.buckets.create(
            name='demo-bucket',
            description='Demo bucket for testing',
            visibility='private'
        )
        print(f"✓ Created bucket: {bucket['name']} (ID: {bucket['id']})")
        
        # List buckets
        print("\n2. Listing buckets...")
        buckets = client.buckets.list()
        print(f"✓ Found {len(buckets)} bucket(s):")
        for b in buckets:
            print(f"  - {b['name']} ({b['visibility']})")
        
        # Create a test file
        test_file_path = 'test_file.txt'
        with open(test_file_path, 'w') as f:
            f.write('Hello from Hypz Python SDK!')
        
        # Upload file (visibility automatically matches bucket)
        print("\n3. Uploading file...")
        file = client.files.upload(
            bucket_id=bucket['id'],
            file_path=test_file_path,
            tags=['demo', 'test'],
            metadata={'source': 'python_sdk_example'}
        )
        print(f"✓ Uploaded file: {file['original_name']}")
        print(f"  URL: {file['url']}")
        print(f"  Size: {file['size']} bytes")
        
        # List files
        print("\n4. Listing files...")
        files = client.files.list(bucket['id'])
        print(f"✓ Found {len(files)} file(s) in bucket:")
        for f in files:
            print(f"  - {f['original_name']} ({f['size']} bytes)")
        
        # Get file details
        print("\n5. Getting file details...")
        file_details = client.files.get(file['id'])
        print(f"✓ File details:")
        print(f"  ID: {file_details['id']}")
        print(f"  Name: {file_details['original_name']}")
        print(f"  MIME Type: {file_details['mime_type']}")
        print(f"  Tags: {file_details['tags']}")
        
        # Download file
        print("\n6. Downloading file...")
        download_path = 'downloaded_file.txt'
        client.files.download(file['id'], save_path=download_path)
        print(f"✓ Downloaded to: {download_path}")
        
        # Update file metadata
        print("\n7. Updating file metadata...")
        updated_file = client.files.update(
            file['id'],
            tags=['demo', 'test', 'updated']
        )
        print(f"✓ Updated tags: {updated_file['tags']}")
        
        # Get bucket stats
        print("\n8. Getting bucket statistics...")
        stats = client.buckets.stats(bucket['id'])
        print(f"✓ Bucket stats:")
        print(f"  Files: {stats['file_count']}")
        print(f"  Total size: {stats['total_size']} bytes")
        
        # Cleanup
        print("\n9. Cleaning up...")
        client.files.delete(file['id'])
        print(f"✓ Deleted file")
        
        client.buckets.delete(bucket['id'])
        print(f"✓ Deleted bucket")
        
        # Remove test files
        os.remove(test_file_path)
        os.remove(download_path)
        print(f"✓ Removed local files")
        
        print("\n✨ Example completed successfully!")
        
    except HypzError as e:
        print(f"\n❌ Error: {e}")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")

if __name__ == '__main__':
    main()
