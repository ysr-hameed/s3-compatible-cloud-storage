/**
 * Comprehensive Node.js SDK Test
 * Tests all SDK methods including newly added usage and bulk operations
 */

const { HypzSDK } = require('./index');

// Configuration
const TEST_API_KEY = process.env.HYPZ_API_KEY || 'test-api-key';
const TEST_BASE_URL = process.env.HYPZ_BASE_URL || 'http://localhost:5000/api/v1';

// Test results tracking
const results = {
  passed: 0,
  failed: 0,
  skipped: 0,
  tests: []
};

function logTest(name, status, error = null) {
  const emoji = status === 'PASS' ? '✅' : status === 'FAIL' ? '❌' : '⏭️';
  console.log(`${emoji} ${name}`);
  
  if (error) {
    console.log(`   Error: ${error.message}`);
  }
  
  results.tests.push({ name, status, error: error?.message });
  if (status === 'PASS') results.passed++;
  else if (status === 'FAIL') results.failed++;
  else results.skipped++;
}

async function runTests() {
  console.log('\n🧪 Starting Node.js SDK Comprehensive Test Suite\n');
  console.log('═'.repeat(60));
  
  let hypz;
  
  // Test 1: SDK Initialization
  try {
    hypz = new HypzSDK(TEST_API_KEY, { baseURL: TEST_BASE_URL });
    logTest('SDK Initialization', 'PASS');
  } catch (error) {
    logTest('SDK Initialization', 'FAIL', error);
    console.log('\n❌ Cannot proceed without SDK initialization');
    printSummary();
    return;
  }
  
  // Test 2: Check managers exist
  try {
    if (!hypz.usage) throw new Error('usage manager not initialized');
    if (!hypz.files) throw new Error('files manager not initialized');
    if (!hypz.buckets) throw new Error('buckets manager not initialized');
    logTest('Manager Initialization', 'PASS');
  } catch (error) {
    logTest('Manager Initialization', 'FAIL', error);
  }
  
  // Test 3: Check usage methods exist
  try {
    if (typeof hypz.usage.current !== 'function') throw new Error('usage.current() not found');
    if (typeof hypz.usage.history !== 'function') throw new Error('usage.history() not found');
    if (typeof hypz.usage.analytics !== 'function') throw new Error('usage.analytics() not found');
    logTest('Usage Manager Methods', 'PASS');
  } catch (error) {
    logTest('Usage Manager Methods', 'FAIL', error);
  }
  
  // Test 4: Check files.bulkDownload exists
  try {
    if (typeof hypz.files.bulkDownload !== 'function') {
      throw new Error('files.bulkDownload() not found');
    }
    logTest('Files Bulk Download Method', 'PASS');
  } catch (error) {
    logTest('Files Bulk Download Method', 'FAIL', error);
  }
  
  // Test 5: Test connection (will fail if backend not running)
  try {
    const result = await hypz.testConnection();
    if (result.success) {
      logTest('Backend Connection', 'PASS');
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    logTest('Backend Connection', 'SKIP', new Error('Backend not running - skipping live tests'));
    console.log('\n⚠️  Backend is not running. Skipping live API tests.\n');
    printSummary();
    return;
  }
  
  // Live API Tests (only if backend is running)
  console.log('\n📡 Running Live API Tests...\n');
  
  let testBucketId = null;
  let testFileId = null;
  
  // Test 6: Create test bucket
  try {
    const result = await hypz.buckets.create({
      name: `test-bucket-${Date.now()}`,
      description: 'SDK Test Bucket',
      visibility: 'private'
    });
    
    if (result.success && result.data) {
      testBucketId = result.data.id;
      logTest('Bucket Creation', 'PASS');
    } else {
      throw new Error('Failed to create bucket');
    }
  } catch (error) {
    logTest('Bucket Creation', 'FAIL', error);
  }
  
  // Test 7: Upload test file
  if (testBucketId) {
    try {
      const testContent = Buffer.from('Test file content for SDK testing');
      const result = await hypz.files.upload(testBucketId, testContent, {
        filename: 'test-file.txt',
        tags: ['test', 'sdk'],
        metadata: { testRun: Date.now().toString() }
      });
      
      if (result.success && result.data) {
        testFileId = result.data.id;
        logTest('File Upload', 'PASS');
      } else {
        throw new Error('Failed to upload file');
      }
    } catch (error) {
      logTest('File Upload', 'FAIL', error);
    }
  } else {
    logTest('File Upload', 'SKIP', new Error('No test bucket'));
  }
  
  // Test 8: Get usage.current()
  try {
    const result = await hypz.usage.current();
    if (result && (result.data || result.storageUsed !== undefined)) {
      logTest('Usage - Current', 'PASS');
    } else {
      throw new Error('Invalid response structure');
    }
  } catch (error) {
    logTest('Usage - Current', 'FAIL', error);
  }
  
  // Test 9: Get usage.history()
  try {
    const result = await hypz.usage.history({ days: 7 });
    if (result && (result.data || Array.isArray(result))) {
      logTest('Usage - History', 'PASS');
    } else {
      throw new Error('Invalid response structure');
    }
  } catch (error) {
    logTest('Usage - History', 'FAIL', error);
  }
  
  // Test 10: Get usage.analytics()
  try {
    const result = await hypz.usage.analytics();
    if (result && (result.data || result.totalApiCalls !== undefined)) {
      logTest('Usage - Analytics', 'PASS');
    } else {
      throw new Error('Invalid response structure');
    }
  } catch (error) {
    logTest('Usage - Analytics', 'FAIL', error);
  }
  
  // Test 11: Bulk download (if we have files)
  if (testFileId) {
    try {
      const result = await hypz.files.bulkDownload([testFileId]);
      if (result && (result.data || result.files)) {
        logTest('Files - Bulk Download', 'PASS');
      } else {
        throw new Error('Invalid response structure');
      }
    } catch (error) {
      logTest('Files - Bulk Download', 'FAIL', error);
    }
  } else {
    logTest('Files - Bulk Download', 'SKIP', new Error('No test file'));
  }
  
  // Cleanup: Delete test bucket
  if (testBucketId) {
    try {
      await hypz.buckets.delete(testBucketId, true);
      logTest('Cleanup - Delete Bucket', 'PASS');
    } catch (error) {
      logTest('Cleanup - Delete Bucket', 'FAIL', error);
    }
  }
  
  printSummary();
}

function printSummary() {
  console.log('\n' + '═'.repeat(60));
  console.log('\n📊 Test Summary\n');
  console.log(`✅ Passed:  ${results.passed}`);
  console.log(`❌ Failed:  ${results.failed}`);
  console.log(`⏭️  Skipped: ${results.skipped}`);
  console.log(`📋 Total:   ${results.tests.length}`);
  
  const successRate = results.tests.length > 0 
    ? ((results.passed / results.tests.length) * 100).toFixed(1) 
    : 0;
  console.log(`\n🎯 Success Rate: ${successRate}%`);
  
  if (results.failed > 0) {
    console.log('\n❌ Failed Tests:');
    results.tests
      .filter(t => t.status === 'FAIL')
      .forEach(t => console.log(`   - ${t.name}: ${t.error}`));
  }
  
  console.log('\n' + '═'.repeat(60) + '\n');
  
  process.exit(results.failed > 0 ? 1 : 0);
}

// Run tests
runTests().catch(error => {
  console.error('\n💥 Unexpected error:', error);
  process.exit(1);
});
