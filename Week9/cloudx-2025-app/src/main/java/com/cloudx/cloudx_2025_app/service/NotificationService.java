package com.cloudx.cloudx_2025_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {
    @Value("${aws.sqs.uploads-notification-queue-url}")
    private String sqsQueueUrl;
    @Value("${aws.sns.uploads-notification-topic-arn}")
    private String snsTopicArn;
    @Value("${PROJECT_NAME}")
    private String projectName;

    private final SqsClient sqsClient;
    private final SnsClient snsClient;
    private final LambdaClient lambdaClient;

    public NotificationService(SqsClient sqsClient, SnsClient snsClient, LambdaClient lambdaClient) {
        this.sqsClient = sqsClient;
        this.snsClient = snsClient;
        this.lambdaClient = lambdaClient;
    }

    public void sendImageUploadMessage(String imageName, long size, String extension, String downloadUrl) {
        String messageBody = String.format("Image uploaded: %s (%d bytes, %s). Download: %s", imageName, size, extension, downloadUrl);
        Map<String, software.amazon.awssdk.services.sqs.model.MessageAttributeValue> attributes = new HashMap<>();
        attributes.put("extension", software.amazon.awssdk.services.sqs.model.MessageAttributeValue.builder()
                .stringValue(extension)
                .dataType("String")
                .build());
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .messageBody(messageBody)
                .messageAttributes(attributes)
                .build();
        sqsClient.sendMessage(request);
    }

    public String subscribeEmail(String email) {
        SubscribeRequest request = SubscribeRequest.builder()
                .topicArn(snsTopicArn)
                .protocol("email-json")
                .endpoint(email)
                .build();
        SubscribeResponse response = snsClient.subscribe(request);
        return response.subscriptionArn();
    }

    public void unsubscribeEmail(String subscriptionArn) {
        UnsubscribeRequest request = UnsubscribeRequest.builder()
                .subscriptionArn(subscriptionArn)
                .build();
        snsClient.unsubscribe(request);
    }

    public String triggerDataConsistencyCheck() {
        String functionName = projectName + "-DataConsistencyFunction";
        String payload = """
            {
                "version": "0",
                "id": "webapp-triggered-event",
                "detail-type": "WebApp",
                "source": "webapp.controller",
                "detail": {}
            }
            """;
        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(software.amazon.awssdk.core.SdkBytes.fromUtf8String(payload))
                .build();
        InvokeResponse response = lambdaClient.invoke(request);
        return new String(response.payload().asByteArray());
    }
}
