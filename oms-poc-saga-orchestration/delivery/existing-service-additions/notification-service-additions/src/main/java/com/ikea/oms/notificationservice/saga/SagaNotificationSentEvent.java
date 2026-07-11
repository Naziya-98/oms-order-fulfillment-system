package com.ikea.oms.notificationservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.notification-sent.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaNotificationSentEvent {

    private Long orderId;
    private String orderNumber;
    private String notificationStatus;
}
