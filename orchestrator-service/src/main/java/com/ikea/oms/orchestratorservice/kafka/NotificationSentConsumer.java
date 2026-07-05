package com.ikea.oms.orchestratorservice.kafka;

import com.ikea.oms.orchestratorservice.event.NotificationSentEvent;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSentConsumer {

    private final SagaOrchestratorService sagaOrchestratorService;

    @KafkaListener(
            topics = "saga.notification-sent.event",
            groupId = "orchestrator-group",
            containerFactory = "notificationSentKafkaListenerFactory"
    )
    public void consume(NotificationSentEvent event, Acknowledgment ack) {

        log.info("[ORCHESTRATOR] NotificationSentEvent received. OrderNumber={}", event.getOrderNumber());

        sagaOrchestratorService.onNotificationSent(event);

        ack.acknowledge();
    }
}