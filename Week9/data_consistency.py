import json
import boto3
import os
from sqlalchemy import create_engine, text

# Initialize S3 client outside handler for reuse across invocations
s3_client = boto3.client('s3')

# Initialize SQLAlchemy engine with connection pooling outside handler
db_url = f"mysql+pymysql://{os.environ['DB_USER']}:{os.environ['DB_PASS']}@{os.environ['DB_HOST']}/{os.environ['DB_NAME']}"
engine = create_engine(
    db_url,
    pool_size=5,
    max_overflow=10,
    pool_timeout=30,
    pool_recycle=3600  # Recycle connections after 1 hour
)

def lambda_handler(event, context):
    """
    Lambda function to perform data consistency checks between RDS and S3.
    Verifies that DB metadata corresponds to images in S3 bucket.
    """
    # Log the incoming event for debugging
    print(f"Incoming event: {json.dumps(event, indent=2)}")

    try:
        bucket_name = os.environ['S3_BUCKET']

        # Query DB for image records using SQLAlchemy
        with engine.connect() as connection:
            result = connection.execute(text("SELECT name FROM images"))
            db_images = [row[0] for row in result.fetchall()]

        # Check consistency
        consistent = True
        missing_in_s3 = []
        extra_in_s3 = []

        # Check DB images exist in S3
        for db_image in db_images:
            try:
                s3_client.head_object(Bucket=bucket_name, Key=db_image)
            except s3_client.exceptions.NoSuchKey:
                missing_in_s3.append(db_image)
                consistent = False

        # List S3 objects and find extras (images in S3 not in DB)
        s3_response = s3_client.list_objects_v2(Bucket=bucket_name)
        s3_files = [obj['Key'] for obj in s3_response.get('Contents', [])] if 'Contents' in s3_response else []

        for s3_file in s3_files:
            if s3_file not in db_images:
                extra_in_s3.append(s3_file)
                consistent = False

        result = {
            'consistent': consistent,
            'db_images_count': len(db_images),
            's3_images_count': len(s3_files),
            'missing_in_s3': missing_in_s3,
            'extra_in_s3': extra_in_s3
        }

        print(f"Data consistency check result: {result}")

        return {
            'statusCode': 200,
            'body': json.dumps(result)
        }

    except Exception as e:
        print(f"Error in data consistency check: {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps({
                'error': str(e),
                'consistent': False
            })
        }
