package com.ikea.oms.orchestratorservice.kafka;
import com.ikea.oms.orchestratorservice.command.CreateOrderCommand;
import com.ikea.oms.orchestratorservice.command.ProcessPaymentCommand;
import com.ikea.oms.orchestratorservice.command.ReleaseInventoryCommand;
import com.ikea.oms.orchestratorservice.command.SendNotificationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// This is the heart of ORCHESTRATION: the orchestrator is the only one
// deciding "what happens next" and telling a downstream service to do it,
// as opposed to choreography where every service reacts on its own.
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCommandProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCreateOrderCommand(CreateOrderCommand command) {

        log.info("[ORCHESTRATOR] Sending CreateOrderCommand. OrderNumber={}", command.getOrderNumber());

        kafkaTemplate.send("saga.create-order.command", command);
    }

    public void sendProcessPaymentCommand(ProcessPaymentCommand command) {

        log.info("[ORCHESTRATOR] Sending ProcessPaymentCommand. OrderNumber={}", command.getOrderNumber());

        kafkaTemplate.send("saga.process-payment.command", command);
    }

    public void sendNotificationCommand(SendNotificationCommand command) {

        log.info("[ORCHESTRATOR] Sending SendNotificationCommand. OrderNumber={}", command.getOrderNumber());

        kafkaTemplate.send("saga.send-notification.command", command);
    }

    public void sendReleaseInventoryCommand(ReleaseInventoryCommand command) {

        log.info("[ORCHESTRATOR] Sending ReleaseInventoryCommand (COMPENSATION). OrderNumber={}", command.getOrderNumber());

        kafkaTemplate.send("saga.release-inventory.command", command);
    }
}
