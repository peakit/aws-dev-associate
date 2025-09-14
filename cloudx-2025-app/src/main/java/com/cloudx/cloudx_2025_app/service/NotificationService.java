package com.cloudx.cloudx_2025_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.HashMap;
import java.util.List;
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

    @Scheduled(fixedRate = 60000)
    public void processSqsMessages() {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .messageAttributeNames("All")
                .build();
        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
        for (Message msg : messages) {
            String body = msg.body();
            String extension = msg.messageAttributes().getOrDefault("extension", software.amazon.awssdk.services.sqs.model.MessageAttributeValue.builder().stringValue("").build()).stringValue();
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .message(body)
                    .messageAttributes(java.util.Collections.singletonMap("extension", software.amazon.awssdk.services.sns.model.MessageAttributeValue.builder().dataType("String").stringValue(extension).build()))
                    .build();
            snsClient.publish(publishRequest);
            sqsClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(sqsQueueUrl).receiptHandle(msg.receiptHandle()).build());
        }
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