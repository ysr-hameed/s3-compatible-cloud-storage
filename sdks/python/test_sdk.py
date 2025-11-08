#!/usr/bin/env python3
"""
Test script for Hypz Python SDK
Run this to verify your API key and test file operations
"""

import sys
import os

# Add the parent directory to path to import hypz module
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from hypz import HypzClient, HypzError

def test_connection(api_key, base_url):
    """Test API connection and operations"""
    print("=" * 60)
    print("Hypz Python SDK - Connection Test")
    print("=" * 60)
    print(f"\nAPI URL: {base_url}")
    print(f"API Key: {api_key[:10]}...{api_key[-4:]}")
    
    try:
        # Initialize client
        print("\n[1/6] Initializing client...")
        client = HypzClient(api_key=api_key, base_url=base_url)
        print("✓ Client initialized")
        
        # Test bucket creation
        print("\n[2/6] Creating test bucket...")
        import time

        bucket = client.buckets.create(
            name=f'sdk-test-bucket-{int(time.time())}',
            description='Test bucket created by Python SDK',
            visibility='private'
        )
        print(f"✓ Bucket created: {bucket['name']} (ID: {bucket['id']})")
        
        # Test bucket listing
        print("\n[3/6] Listing buckets...")
        buckets = client.buckets.list()
        print(f"✓ Found {len(buckets)} bucket(s)")
        
        # Create and upload test file
        print("\n[4/6] Creating and uploading test file...")
        test_file = 'sdk_test_file.txt'
        with open(test_file, 'w') as f:
            f.write('Hello from Hypz Python SDK!\nTimestamp: ' + str(os.times()))
        
        file = client.files.upload(
            bucket_id=bucket['id'],
            file_path=test_file,
            tags=['test', 'sdk'],
            metadata={'source': 'sdk_test'}
        )
        print(f"✓ File uploaded: {file['original_name']}")
        print(f"  File ID: {file['id']}")
        print(f"  Size: {file['size']} bytes")
        print(f"  URL: {file['url']}")
        
        # Test file listing
        print("\n[5/6] Listing files...")
        files = client.files.list(bucket['id'])
        print(f"✓ Found {len(files)} file(s) in bucket")
        
        # Cleanup
        print("\n[6/6] Cleaning up...")
        client.files.delete(file['id'])
        print("✓ File deleted")
        
        client.buckets.delete(bucket['id'])
        print("✓ Bucket deleted")
        
        os.remove(test_file)
        print("✓ Local test file removed")
        
        # Success
        print("\n" + "=" * 60)
        print("✨ ALL TESTS PASSED!")
        print("=" * 60)
        print("\nYour API key is working correctly!")
        print("You can now use the Hypz Python SDK in your projects.")
        return True
        
    except HypzError as e:
        print(f"\n❌ API Error: {e}")
        print("\nPlease check:")
        print("  1. Your API key is correct")
        print("  2. The backend server is running")
        print("  3. The API URL is correct")
        return False
        
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    # Get configuration
    api_key = os.getenv('HYPZ_API_KEY')
    base_url = os.getenv('HYPZ_API_URL', 'http://localhost:5000/api/v1')
    
    # Check if API key is provided
    if not api_key:
        print("ERROR: API key not found!")
        print("\nPlease set your API key:")
        print("  export HYPZ_API_KEY='your_api_key_here'")
        print("\nOr run with:")
        print("  HYPZ_API_KEY='your_key' python test_sdk.py")
        sys.exit(1)
    
    # Run test
    success = test_connection(api_key, base_url)
    sys.exit(0 if success else 1)
