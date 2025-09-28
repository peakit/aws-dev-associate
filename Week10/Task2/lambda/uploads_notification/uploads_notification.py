import json
import boto3
import os
from typing import Dict, Any

sns_client = boto3.client('sns')

def lambda_handler(event: Dict[str, Any], context: Any) -> Dict[str, Any]:
    """
    Lambda function to process SQS messages and forward them to SNS topic.
    Triggered by SQS queue messages.
    """
    print("Received event:", json.dumps(event, indent=2))
    try:
        # Get SNS topic ARN from environment variable
        sns_topic_arn = os.environ['SNS_TOPIC_ARN']

        processed_messages = 0
        failed_messages = 0

        # Process each SQS message record
        for record in event['Records']:
            try:
                # Extract message body and attributes from SQS message
                message_body = record['body']
                message_attributes = record.get('messageAttributes', {})

                # Extract extension attribute
                extension = ''
                if 'extension' in message_attributes:
                    extension = message_attributes['extension']['stringValue']

                # Prepare SNS publish request
                publish_request = {
                    'TopicArn': sns_topic_arn,
                    'Message': message_body,
                    'MessageAttributes': {
                        'extension': {
                            'DataType': 'String',
                            'StringValue': extension
                        }
                    }
                }

                # Publish message to SNS
                sns_client.publish(**publish_request)
                processed_messages += 1

                print(f"Successfully processed message: {message_body[:100]}...")

            except Exception as e:
                print(f"Error processing message: {str(e)}")
                failed_messages += 1
                # Continue processing other messages even if one fails

        print(f"Processed {processed_messages} messages successfully, {failed_messages} failed")

        return {
            'statusCode': 200,
            'body': json.dumps({
                'processed': processed_messages,
                'failed': failed_messages
            })
        }

    except Exception as e:
        print(f"Lambda execution error: {str(e)}")
        raise e