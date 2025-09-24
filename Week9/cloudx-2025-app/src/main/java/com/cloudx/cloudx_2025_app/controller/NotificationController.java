package com.cloudx.cloudx_2025_app.controller;

import com.cloudx.cloudx_2025_app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String email) {
        return notificationService.subscribeEmail(email);
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String subscriptionArn) {
        notificationService.unsubscribeEmail(subscriptionArn);
        return "Unsubscribed";
    }

    @PostMapping("/data-consistency")
    public String triggerDataConsistency() {
        return notificationService.triggerDataConsistencyCheck();
    }
}
