"""
Bulk file upload example with progress tracking
"""

from hypz import HypzClient, HypzError
import os
import time
from pathlib import Path

API_KEY = os.getenv('HYPZ_API_KEY', 'your_api_key_here')
BASE_URL = os.getenv('HYPZ_API_URL', 'http://localhost:5000/api/v1')

def create_test_files(count=5):
    """Create test files for uploading"""
    test_dir = Path('test_files')
    test_dir.mkdir(exist_ok=True)
    
    files = []
    for i in range(count):
        file_path = test_dir / f'test_file_{i+1}.txt'
        with open(file_path, 'w') as f:
            f.write(f'Test file {i+1}\n' * 100)
        files.append(str(file_path))
    
    return files

def main():
    print("Bulk File Upload Example")
    print("=" * 50)
    
    client = HypzClient(api_key=API_KEY, base_url=BASE_URL)
    
    try:
        # Create a bucket
        print("\n1. Creating bucket...")
        bucket = client.buckets.create(
            name='bulk-upload-demo',
            description='Bulk upload demonstration'
        )
        print(f"✓ Created bucket: {bucket['name']}")
        
        # Create test files
        print("\n2. Creating test files...")
        test_files = create_test_files(5)
        print(f"✓ Created {len(test_files)} test files")
        
        # Upload files with progress
        print("\n3. Uploading files...")
        uploaded_files = []
        
        for i, file_path in enumerate(test_files, 1):
            print(f"\nUploading file {i}/{len(test_files)}: {Path(file_path).name}")
            
            start_time = time.time()
            file = client.files.upload(
                bucket_id=bucket['id'],
                file_path=file_path,
                tags=['bulk', f'file_{i}'],
                metadata={'batch': 'demo_batch', 'index': i}
            )
            elapsed = time.time() - start_time
            
            uploaded_files.append(file)
            print(f"✓ Uploaded in {elapsed:.2f}s - {file['original_name']}")
        
        # List all uploaded files
        print(f"\n4. Listing all uploaded files...")
        files = client.files.list(bucket['id'], limit=50)
        print(f"✓ Total files in bucket: {len(files)}")
        
        total_size = sum(f['size'] for f in files)
        print(f"✓ Total size: {total_size:,} bytes ({total_size/1024:.2f} KB)")
        
        # Get bucket stats
        stats = client.buckets.stats(bucket['id'])
        print(f"\n5. Bucket statistics:")
        print(f"  Files: {stats['file_count']}")
        print(f"  Size: {stats['total_size']:,} bytes")
        
        # Cleanup
        print(f"\n6. Cleaning up...")
        
        # Delete uploaded files
        for file in uploaded_files:
            client.files.delete(file['id'])
        print(f"✓ Deleted {len(uploaded_files)} files from bucket")
        
        # Delete bucket
        client.buckets.delete(bucket['id'])
        print(f"✓ Deleted bucket")
        
        # Remove test files
        for file_path in test_files:
            os.remove(file_path)
        os.rmdir('test_files')
        print(f"✓ Removed local test files")
        
        print("\n✨ Bulk upload example completed!")
        
    except HypzError as e:
        print(f"\n❌ Error: {e}")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")

if __name__ == '__main__':
    main()
