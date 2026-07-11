package com.ikea.oms.paymentservice.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Simulates a real payment gateway.
 *
 * This replaces the earlier approach where the flow was triggered by
 * checking `skuCode.equals("FAIL_PAYMENT")`. A real gateway does not know
 * or care what SKU is being purchased - it authorizes/declines based on
 * its own logic and always takes some non-zero time to respond. This class
 * mimics that: it responds asynchronously after a configurable delay
 * (payment.simulation.delay-ms) and approves/declines based on a
 * configurable probability (payment.simulation.failure-rate), instead of
 * business data smuggled in through the order payload.
 */
@Slf4j
@Service
public class PaymentGatewaySimulator {

    @Value("${payment.simulation.delay-ms:3000}")
    private long simulatedDelayMs;

    @Value("${payment.simulation.failure-rate:0.2}")
    private double failureRate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /**
     * Kicks off an asynchronous "authorization call" to the gateway.
     * onResult is invoked once the simulated network/processing delay has
     * elapsed, with true = approved, false = declined.
     */
    public void authorizeAsync(String orderNumber, Consumer<Boolean> onResult) {

        log.info(
                "Gateway: authorization request sent for OrderNumber={}. Simulated processing time={}ms",
                orderNumber,
                simulatedDelayMs
        );

        scheduler.schedule(() -> {
            try {
                boolean approved = ThreadLocalRandom.current().nextDouble() >= failureRate;

                log.info(
                        "Gateway: authorization response for OrderNumber={} -> {}",
                        orderNumber,
                        approved ? "APPROVED" : "DECLINED"
                );

                onResult.accept(approved);
            } catch (Exception ex) {
                log.error("Gateway: unexpected error while processing OrderNumber={}", orderNumber, ex);
                onResult.accept(false);
            }
        }, simulatedDelayMs, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
