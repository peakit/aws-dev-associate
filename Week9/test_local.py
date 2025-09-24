#!/usr/bin/env python
"""
Simple local test script for the Lambda function.
This script simulates the Lambda environment for testing.
"""

import json
import os
from lambda_function import lambda_handler

def test_lambda_function():
    """Test the Lambda function with the test event."""

    print("=== Lambda Function Local Test ===\n")

    # Load test event
    try:
        with open('test-event.json', 'r') as f:
            test_event = json.load(f)
        print("✅ Test event loaded successfully")
    except FileNotFoundError:
        print("❌ test-event.json not found. Please ensure the file exists.")
        return
    except json.JSONDecodeError as e:
        print(f"❌ Invalid JSON in test-event.json: {e}")
        return

    # Set mock environment variable (required by Lambda function)
    os.environ['SNS_TOPIC_ARN'] = 'arn:aws:sns:us-east-1:123456789012:test-topic'
    print("✅ Environment variables set")

    # Mock Lambda context (minimal implementation)
    class MockContext:
        def __init__(self):
            self.function_name = "test-function"
            self.memory_limit_in_mb = 128
            self.invoked_function_arn = "arn:aws:lambda:us-east-1:123456789012:function:test"
            self.aws_request_id = "test-request-123"

    context = MockContext()

    print(f"📋 Testing with {len(test_event['Records'])} SQS message(s)\n")

    # Execute the Lambda function
    try:
        print("🚀 Executing Lambda function...")
        result = lambda_handler(test_event, context)

        print("✅ Lambda function executed successfully!")
        print("\n📤 Function Result:")
        print(json.dumps(result, indent=2))

        # Validate result structure
        if 'statusCode' in result and result['statusCode'] == 200:
            body = json.loads(result['body'])
            print("
📊 Processing Summary:"            print(f"   Messages processed: {body['processed']}")
            print(f"   Messages failed: {body['failed']}")
        else:
            print("⚠️  Unexpected result format")

    except Exception as e:
        print(f"❌ Lambda function failed: {e}")
        import traceback
        traceback.print_exc()

    print("\n=== Test Complete ===")

if __name__ == "__main__":
    test_lambda_function()
