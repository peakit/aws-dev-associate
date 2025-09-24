package com.cloudx.cloudx_2025_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {
    @Value("${aws.sqs.uploads-notification-queue-url}")
    private String sqsQueueUrl;
    @Value("${aws.sns.uploads-notification-topic-arn}")
    private String snsTopicArn;

    private final SqsClient sqsClient;
    private final SnsClient snsClient;

    public NotificationService(SqsClient sqsClient, SnsClient snsClient) {
        this.sqsClient = sqsClient;
        this.snsClient = snsClient;
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
                .protocol("email")
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
}
