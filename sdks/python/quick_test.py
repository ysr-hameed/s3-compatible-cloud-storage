#!/usr/bin/env python3
"""
Quick test script to verify Hypz Python SDK
Run this to check if everything is working
"""

import os
import sys

# Add current directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

try:
    from hypz import HypzClient, HypzError
    print("✓ Hypz SDK imported successfully")
except ImportError as e:
    print(f"✗ Failed to import Hypz SDK: {e}")
    print("\nTry running: pip install requests")
    sys.exit(1)

# Get API key from environment
API_KEY = os.getenv('HYPZ_API_KEY')

if not API_KEY:
    print("\n" + "="*60)
    print("⚠️  API KEY NOT SET")
    print("="*60)
    print("\nPlease set your API key:")
    print("  export HYPZ_API_KEY='your_api_key_here'")
    print("\nTo get an API key:")
    print("  1. Open http://localhost:5173")
    print("  2. Login to dashboard")
    print("  3. Go to API Keys section")
    print("  4. Click 'Generate New API Key'")
    print("  5. Copy the key and run:")
    print("     export HYPZ_API_KEY='<your_key>'")
    print()
    sys.exit(1)

print(f"\n✓ API Key found: {API_KEY[:10]}...{API_KEY[-4:]}")
print(f"✓ Backend URL: http://localhost:5000/api/v1")

print("\n" + "="*60)
print("Testing connection...")
print("="*60)

try:
    client = HypzClient(api_key=API_KEY)
    
    # Try to list buckets
    buckets = client.buckets.list()
    print(f"\n✅ SUCCESS! Connected to Hypz API")
    print(f"✓ Found {len(buckets)} bucket(s)")
    
    if buckets:
        print("\nYour buckets:")
        for bucket in buckets:
            print(f"  - {bucket['name']} ({bucket['visibility']})")
    
    print("\n" + "="*60)
    print("🎉 Everything is working!")
    print("="*60)
    print("\nNext steps:")
    print("  • Run: python examples/basic_usage.py")
    print("  • Check: README.md for full documentation")
    print("  • View: examples/ folder for more examples")
    print()
    
except HypzError as e:
    print(f"\n❌ API Error: {e}")
    print("\nTroubleshooting:")
    print("  1. Is the backend running? (cd backend && npm start)")
    print("  2. Is your API key correct?")
    print("  3. Does the key have read permissions?")
    print()
    sys.exit(1)
    
except Exception as e:
    print(f"\n❌ Unexpected error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
