"""
File metadata management example
"""

from hypz import HypzClient, HypzError
import os
import json

API_KEY = os.getenv('HYPZ_API_KEY', 'your_api_key_here')
BASE_URL = os.getenv('HYPZ_API_URL', 'http://localhost:5000/api/v1')

def main():
    print("File Metadata Example")
    print("=" * 50)
    
    client = HypzClient(api_key=API_KEY, base_url=BASE_URL)
    
    try:
        # Create bucket
        print("\n1. Creating bucket...")
        bucket = client.buckets.create(name='metadata-demo')
        print(f"✓ Created bucket: {bucket['id']}")
        
        # Create a test file with rich metadata
        test_file = 'document.txt'
        with open(test_file, 'w') as f:
            f.write('Important document content')
        
        # Upload with comprehensive metadata (visibility matches bucket)
        print("\n2. Uploading file with metadata...")
        file = client.files.upload(
            bucket_id=bucket['id'],
            file_path=test_file,
            tags=['document', 'important', 'q4-2024'],
            metadata={
                'department': 'Engineering',
                'project': 'Project Alpha',
                'author': 'John Doe',
                'version': '1.0',
                'status': 'draft',
                'created_by': 'python_sdk'
            }
        )
        print(f"✓ Uploaded: {file['original_name']}")
        print(f"\nInitial metadata:")
        print(json.dumps(file['metadata'], indent=2))
        print(f"\nInitial tags: {file['tags']}")
        
        # Update metadata
        print("\n3. Updating file metadata...")
        updated = client.files.update(
            file['id'],
            tags=['document', 'important', 'q4-2024', 'reviewed'],
            metadata={
                'department': 'Engineering',
                'project': 'Project Alpha',
                'author': 'John Doe',
                'version': '1.1',
                'status': 'reviewed',
                'reviewed_by': 'Jane Smith',
                'review_date': '2024-11-03'
            }
        )
        print(f"✓ Updated metadata:")
        print(json.dumps(updated['metadata'], indent=2))
        print(f"\nUpdated tags: {updated['tags']}")
        
        # Retrieve and display
        print("\n4. Retrieving file details...")
        file_info = client.files.get(file['id'])
        print(f"\nFile Information:")
        print(f"  Name: {file_info['original_name']}")
        print(f"  Size: {file_info['size']} bytes")
        print(f"  MIME: {file_info['mime_type']}")
        print(f"  Public: {file_info['is_public']}")
        print(f"  Tags: {', '.join(file_info['tags'])}")
        print(f"  Metadata fields: {len(file_info['metadata'])}")
        
        # Cleanup
        print("\n5. Cleaning up...")
        client.files.delete(file['id'])
        client.buckets.delete(bucket['id'])
        os.remove(test_file)
        print("✓ Cleanup complete")
        
        print("\n✨ Metadata example completed!")
        
    except HypzError as e:
        print(f"\n❌ Error: {e}")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")

if __name__ == '__main__':
    main()
