package com.ikea.oms.orchestratorservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.send-notification.command
// Consumed by: notification-service (SagaSendNotificationCommandConsumer)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationCommand {

    private Long orderId;
    private String orderNumber;
}
