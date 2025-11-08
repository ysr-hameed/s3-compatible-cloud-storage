"""
Comprehensive Python SDK Test
Tests all SDK methods including newly added usage and bulk operations
"""

import os
import sys
from datetime import datetime

# Add SDK to path
sys.path.insert(0, os.path.dirname(__file__))

try:
    from hypz import HypzClient, HypzError
except ImportError as e:
    print(f"❌ Failed to import SDK: {e}")
    sys.exit(1)

# Configuration
TEST_API_KEY = os.environ.get('HYPZ_API_KEY', 'test-api-key')
TEST_BASE_URL = os.environ.get('HYPZ_BASE_URL', 'http://localhost:5000/api/v1')

# Test results tracking
results = {
    'passed': 0,
    'failed': 0,
    'skipped': 0,
    'tests': []
}

def log_test(name, status, error=None):
    """Log test result"""
    emoji = '✅' if status == 'PASS' else '❌' if status == 'FAIL' else '⏭️'
    print(f"{emoji} {name}")
    
    if error:
        print(f"   Error: {error}")
    
    results['tests'].append({
        'name': name,
        'status': status,
        'error': str(error) if error else None
    })
    
    if status == 'PASS':
        results['passed'] += 1
    elif status == 'FAIL':
        results['failed'] += 1
    else:
        results['skipped'] += 1

def print_summary():
    """Print test summary"""
    print('\n' + '═' * 60)
    print('\n📊 Test Summary\n')
    print(f"✅ Passed:  {results['passed']}")
    print(f"❌ Failed:  {results['failed']}")
    print(f"⏭️  Skipped: {results['skipped']}")
    print(f"📋 Total:   {len(results['tests'])}")
    
    total = len(results['tests'])
    success_rate = (results['passed'] / total * 100) if total > 0 else 0
    print(f"\n🎯 Success Rate: {success_rate:.1f}%")
    
    if results['failed'] > 0:
        print('\n❌ Failed Tests:')
        for test in results['tests']:
            if test['status'] == 'FAIL':
                print(f"   - {test['name']}: {test['error']}")
    
    print('\n' + '═' * 60 + '\n')
    
    sys.exit(1 if results['failed'] > 0 else 0)

def run_tests():
    """Run all tests"""
    print('\n🧪 Starting Python SDK Comprehensive Test Suite\n')
    print('═' * 60)
    
    client = None
    
    # Test 1: SDK Initialization
    try:
        client = HypzClient(api_key=TEST_API_KEY, base_url=TEST_BASE_URL)
        log_test('SDK Initialization', 'PASS')
    except Exception as error:
        log_test('SDK Initialization', 'FAIL', error)
        print('\n❌ Cannot proceed without SDK initialization')
        print_summary()
        return
    
    # Test 2: Check managers exist
    try:
        if not hasattr(client, 'usage'):
            raise Exception('usage manager not initialized')
        if not hasattr(client, 'files'):
            raise Exception('files manager not initialized')
        if not hasattr(client, 'buckets'):
            raise Exception('buckets manager not initialized')
        log_test('Manager Initialization', 'PASS')
    except Exception as error:
        log_test('Manager Initialization', 'FAIL', error)
    
    # Test 3: Check usage methods exist
    try:
        if not hasattr(client.usage, 'current'):
            raise Exception('usage.current() not found')
        if not hasattr(client.usage, 'history'):
            raise Exception('usage.history() not found')
        if not hasattr(client.usage, 'analytics'):
            raise Exception('usage.analytics() not found')
        log_test('Usage Manager Methods', 'PASS')
    except Exception as error:
        log_test('Usage Manager Methods', 'FAIL', error)
    
    # Test 4: Check files.bulk_download exists
    try:
        if not hasattr(client.files, 'bulk_download'):
            raise Exception('files.bulk_download() not found')
        log_test('Files Bulk Download Method', 'PASS')
    except Exception as error:
        log_test('Files Bulk Download Method', 'FAIL', error)
    
    # Test 5: Test connection (will fail if backend not running)
    try:
        result = client.buckets.list(limit=1)
        log_test('Backend Connection', 'PASS')
    except Exception as error:
        log_test('Backend Connection', 'SKIP', Exception('Backend not running - skipping live tests'))
        print('\n⚠️  Backend is not running. Skipping live API tests.\n')
        print_summary()
        return
    
    # Live API Tests (only if backend is running)
    print('\n📡 Running Live API Tests...\n')
    
    test_bucket_id = None
    test_file_id = None
    
    # Test 6: Create test bucket
    try:
        result = client.buckets.create(
            name=f'test-bucket-{int(datetime.now().timestamp())}',
            description='SDK Test Bucket',
            visibility='private'
        )
        
        if result and 'id' in result:
            test_bucket_id = result['id']
            log_test('Bucket Creation', 'PASS')
        else:
            raise Exception('Failed to create bucket')
    except Exception as error:
        log_test('Bucket Creation', 'FAIL', error)
    
    # Test 7: Upload test file
    if test_bucket_id:
        try:
            test_content = b'Test file content for SDK testing'
            
            # Create temporary file
            import tempfile
            with tempfile.NamedTemporaryFile(mode='wb', delete=False, suffix='.txt') as f:
                f.write(test_content)
                temp_file = f.name
            
            result = client.files.upload(
                bucket_id=test_bucket_id,
                file_path=temp_file,
                tags=['test', 'sdk'],
                metadata={'testRun': str(int(datetime.now().timestamp()))}
            )
            
            # Cleanup temp file
            os.unlink(temp_file)
            
            if result and 'id' in result:
                test_file_id = result['id']
                log_test('File Upload', 'PASS')
            else:
                raise Exception('Failed to upload file')
        except Exception as error:
            log_test('File Upload', 'FAIL', error)
    else:
        log_test('File Upload', 'SKIP', Exception('No test bucket'))
    
    # Test 8: Get usage.current()
    try:
        result = client.usage.current()
        if result and ('storageUsed' in result or 'storage_used' in result):
            log_test('Usage - Current', 'PASS')
        else:
            raise Exception('Invalid response structure')
    except Exception as error:
        log_test('Usage - Current', 'FAIL', error)
    
    # Test 9: Get usage.history()
    try:
        result = client.usage.history(days=7)
        if result and (isinstance(result, list) or 'data' in result):
            log_test('Usage - History', 'PASS')
        else:
            raise Exception('Invalid response structure')
    except Exception as error:
        log_test('Usage - History', 'FAIL', error)
    
    # Test 10: Get usage.analytics()
    try:
        result = client.usage.analytics()
        if result:
            log_test('Usage - Analytics', 'PASS')
        else:
            raise Exception('Invalid response structure')
    except Exception as error:
        log_test('Usage - Analytics', 'FAIL', error)
    
    # Test 11: Bulk download (if we have files)
    if test_file_id:
        try:
            result = client.files.bulk_download([test_file_id])
            if result and ('files' in result or 'data' in result):
                log_test('Files - Bulk Download', 'PASS')
            else:
                raise Exception('Invalid response structure')
        except Exception as error:
            log_test('Files - Bulk Download', 'FAIL', error)
    else:
        log_test('Files - Bulk Download', 'SKIP', Exception('No test file'))
    
    # Cleanup: Delete test bucket
    if test_bucket_id:
        try:
            client.buckets.delete(test_bucket_id, force=True)
            log_test('Cleanup - Delete Bucket', 'PASS')
        except Exception as error:
            log_test('Cleanup - Delete Bucket', 'FAIL', error)
    
    print_summary()

if __name__ == '__main__':
    try:
        run_tests()
    except Exception as error:
        print(f'\n💥 Unexpected error: {error}')
        import traceback
        traceback.print_exc()
        sys.exit(1)
