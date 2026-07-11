package com.ikea.oms.notificationservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Consumed from topic: saga.send-notification.command (sent by orchestrator-service)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationCommand {

    private Long orderId;
    private String orderNumber;
}
