package com.ikea.oms.orchestratorservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Consumed from topic: saga.notification-sent.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSentEvent {

    private Long orderId;
    private String orderNumber;
    private String notificationStatus;
}
