import json

def lambda_handler(event, context):
    # Print the full event for debugging
    print("Received event:", json.dumps(event))
    # Loop through records and print the S3 object key
    for record in event.get('Records', []):
        s3_object = record.get('s3', {}).get('object', {})
        object_key = s3_object.get('key')
        print(f"S3 object created: {object_key}")
    return {"statusCode": 200, "body": "Processed S3 event"}