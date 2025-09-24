# Local SAM Testing Guide for IntelliJ IDEA

This guide will help you set up AWS SAM locally to test the Lambda function with dummy events.

## Prerequisites

1. **AWS CLI 2.2.25** (you already have this)
2. **Python 3.9+**
3. **Docker** (for local Lambda execution)
4. **AWS SAM CLI**

## Step 1: Install AWS SAM CLI

### For Windows (using PowerShell as Administrator):

```powershell
# Download and install SAM CLI
Invoke-WebRequest -Uri "https://github.com/aws/aws-sam-cli/releases/latest/download/AWS_SAM_CLI_64_PY3.msi" -OutFile "AWS_SAM_CLI.msi"
Start-Process msiexec.exe -Wait -ArgumentList "/i AWS_SAM_CLI.msi /quiet"

# Verify installation
sam --version
```

### Alternative: Using Chocolatey (if you have it):

```powershell
choco install aws-sam-cli
```

## Step 2: Install Docker

Download and install Docker Desktop for Windows from: https://www.docker.com/products/docker-desktop

## Step 3: Configure AWS Credentials

Make sure your AWS credentials are configured:

```bash
aws configure
# Enter your AWS Access Key ID, Secret Access Key, region (ap-south-1), and output format
```

## Step 4: Set Up Project Structure

Your Week9 directory should now have these files:
- `lambda_function.py` - Lambda function code
- `template.yaml` - SAM template
- `test-event.json` - Test event data
- `requirements.txt` - Python dependencies

## Step 5: Install Python Dependencies

In IntelliJ IDEA Terminal (View → Tool Windows → Terminal):

```bash
cd Week9
pip install -r requirements.txt
```

## Step 6: Test Lambda Function Locally

### Method 1: Direct Python Testing (Simple)

Create a simple test script to verify your function works:

```python
# Create test_local.py
import json
from lambda_function import lambda_handler

# Load test event
with open('test-event.json', 'r') as f:
    test_event = json.load(f)

# Mock environment variable
import os
os.environ['SNS_TOPIC_ARN'] = 'arn:aws:sns:us-east-1:123456789012:test-topic'

# Test the function
try:
    result = lambda_handler(test_event, {})
    print("Function executed successfully!")
    print("Result:", json.dumps(result, indent=2))
except Exception as e:
    print(f"Error: {e}")
```

Run it:
```bash
python test_local.py
```

### Method 2: Using SAM CLI (Recommended)

#### Build the function:
```bash
sam build
```

#### Invoke locally with test event:
```bash
sam local invoke UploadsNotificationFunction --event test-event.json
```

#### Start local API (if you had API Gateway):
```bash
sam local start-api
```

#### Start local Lambda with event source (for SQS testing):
```bash
sam local start-lambda
```

## Step 7: IntelliJ IDEA Integration

### Configure Python Interpreter

1. **File → Project Structure → Project SDK**
2. Add Python 3.9+ interpreter
3. Set project SDK

### Create Run Configurations

1. **Run → Edit Configurations**
2. **Add new configuration → Python**
3. Set:
   - Script path: `test_local.py`
   - Working directory: `Week9`
   - Python interpreter: Your Python 3.9+

### Enable Terminal Integration

- **View → Tool Windows → Terminal**
- Use this terminal for SAM commands

## Step 8: Debugging Lambda Function

### Add Debug Prints

Modify your `lambda_function.py` to add debug information:

```python
def lambda_handler(event, context):
    print("=== LAMBDA DEBUG START ===")
    print(f"Event: {json.dumps(event, indent=2)}")
    print(f"Context: {context}")

    # Your existing code...

    print("=== LAMBDA DEBUG END ===")
    return result
```

### Test with Different Events

Create multiple test events:

```bash
# test-event-single.json
{
  "Records": [
    {
      "messageId": "test-1",
      "receiptHandle": "test-handle",
      "body": "Image uploaded: test.jpg (1000 bytes, jpg). Download: /test.jpg",
      "messageAttributes": {
        "extension": {
          "stringValue": "jpg",
          "dataType": "String"
        }
      },
      "eventSource": "aws:sqs",
      "awsRegion": "us-east-1"
    }
  ]
}
```

## Step 9: Common Issues & Solutions

### Issue: "docker: command not found"
**Solution**: Install Docker Desktop and ensure it's running

### Issue: "sam: command not found"
**Solution**: Restart your terminal or add SAM to PATH

### Issue: "Runtime.ImportModuleError"
**Solution**: Check your requirements.txt and ensure dependencies are compatible

### Issue: "The security token included in the request is invalid"
**Solution**: Check your AWS credentials with `aws sts get-caller-identity`

### Issue: Permission denied errors
**Solution**: Run terminal as administrator or check file permissions

## Step 10: Advanced Testing

### Test with Real AWS Services

For more realistic testing, you can deploy just the SNS topic to AWS and test against real SNS:

```bash
# Deploy only SNS topic
aws cloudformation deploy \
  --template-file template.yaml \
  --stack-name test-sns-stack \
  --capabilities CAPABILITY_IAM \
  --parameter-overrides ProjectName="test"

# Get the topic ARN
TOPIC_ARN=$(aws cloudformation describe-stacks \
  --stack-name test-sns-stack \
  --query 'Stacks[0].Outputs[?OutputKey==`UploadsNotificationTopicArn`].OutputValue' \
  --output text)

# Update your test script to use real ARN
export SNS_TOPIC_ARN=$TOPIC_ARN
```

## Step 11: Cleanup

After testing:

```bash
# Remove test stack
aws cloudformation delete-stack --stack-name test-sns-stack

# Clean up Docker images
docker system prune -f
```

## IntelliJ Tips

- Use **Ctrl+Shift+F10** to run current file
- Use **Ctrl+Shift+A** to find actions (search for "Terminal")
- Install Python plugin if not already installed
- Use **File → Settings → Tools → Terminal** to customize terminal

## Next Steps

Once local testing is successful:
1. Deploy to AWS using CloudFormation
2. Test with real SQS messages
3. Monitor using CloudWatch
4. Set up email subscriptions to SNS topic
