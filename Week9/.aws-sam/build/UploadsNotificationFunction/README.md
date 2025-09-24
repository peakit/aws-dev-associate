# Week 9: Serverless Upload Notifications

This directory contains the updated CloudFormation templates and application code for implementing serverless upload notifications using AWS Lambda.

## Changes Made

### 1. Lambda Function Implementation
- **File**: `lambda_function.py`
- **Function Name**: `${ProjectName}-UploadsNotificationFunction`
- **Purpose**: Processes SQS messages and forwards them to SNS topic
- **Trigger**: SQS queue messages (batch size: 10)

### 2. CloudFormation Updates (`app-stack.yml`)
- Added Lambda IAM role with SNS publish permissions
- Added Lambda function with inline Python code
- Added SQS event source mapping to trigger Lambda
- Removed SQS ARN environment variable from EC2 user data (no longer needed for polling)

### 3. Java Application Updates
- **File**: `cloudx-2025-app/src/main/java/com/cloudx/cloudx_2025_app/service/NotificationService.java`
  - Removed `@Scheduled` annotation and `processSqsMessages()` method
  - Application still sends messages to SQS but no longer polls them

- **File**: `cloudx-2025-app/src/main/resources/application.properties`
  - Removed `aws.sqs.uploads-notification-queue-arn` property

## Architecture Flow

1. User uploads image via web application
2. `ImageService.upload()` calls `NotificationService.sendImageUploadMessage()`
3. Message is sent to SQS queue with image details
4. SQS triggers Lambda function automatically
5. Lambda processes message and publishes to SNS topic
6. Email subscribers receive notifications

## Deployment Instructions

### Prerequisites
- AWS CLI configured with appropriate permissions
- Existing network stack deployed
- Application JAR file built and uploaded to S3

### Deploy the Application Stack

```bash
# Set your project name (replace with actual value)
PROJECT_NAME="cloudx-2025"

# Deploy the app stack (update parameters as needed)
aws cloudformation deploy \
  --template-file app-stack.yml \
  --stack-name ${PROJECT_NAME}-app-stack \
  --parameter-overrides \
    ProjectName=${PROJECT_NAME} \
    AppJarBucket="your-app-jar-bucket" \
    AppJarKey="cloudx2025-app.jar" \
    RDSHost="your-rds-endpoint" \
    DBUsername="your-db-username" \
    DBPassword="your-db-password" \
    S3BucketName="your-s3-bucket" \
    DBName="cloudx2025" \
  --capabilities CAPABILITY_NAMED_IAM
```

### Testing

1. **Subscribe to Notifications**
   - Access your web application
   - Go to the notification subscription page
   - Subscribe an email address to receive notifications

2. **Upload Images**
   - Upload at least 2 images through the web application
   - Verify that email notifications are received for each upload

3. **Verify Lambda Execution**
   - Check CloudWatch Logs for the Lambda function
   - Confirm messages are processed successfully

## Key Benefits

- **Event-Driven**: Notifications are processed immediately when messages arrive in SQS
- **Scalable**: Lambda scales automatically based on message volume
- **Cost-Effective**: Pay only for actual message processing
- **Reliable**: Built-in retry mechanisms and dead letter queues available
- **Maintainable**: Separation of concerns between application and notification processing

## Monitoring

- **Lambda Metrics**: Check AWS Lambda console for invocation counts, duration, and errors
- **SQS Metrics**: Monitor queue depth and message processing rates
- **CloudWatch Logs**: Review Lambda function logs for detailed execution information

## Troubleshooting

1. **Lambda Not Triggering**: Check SQS event source mapping status
2. **Permission Errors**: Verify IAM role has correct SNS publish permissions
3. **Email Not Received**: Confirm SNS topic subscriptions are confirmed
4. **Messages Not Processing**: Check CloudWatch logs for Lambda errors
