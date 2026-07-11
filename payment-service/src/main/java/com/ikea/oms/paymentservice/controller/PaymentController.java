package com.ikea.oms.paymentservice.controller;

import com.ikea.oms.paymentservice.dto.PaymentResponseDTO;
import com.ikea.oms.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public List<PaymentResponseDTO> getAllPayments() {
        log.info("Fetching all payment records");
        return paymentService.getAllPayments();
    }

    @GetMapping("/{orderNumber}")
    public PaymentResponseDTO getPayment(@PathVariable String orderNumber) {
        log.info("Fetching payment for OrderNumber={}", orderNumber);
        return paymentService.getPaymentByOrderNumber(orderNumber);
    }

    // Manual compensating action - cancel/reverse a payment directly via
    // Postman instead of relying on a magic skuCode value to fail it.
    @PostMapping("/{orderNumber}/refund")
    public PaymentResponseDTO refundPayment(@PathVariable String orderNumber) {
        log.info("Manual payment refund (compensation) requested for OrderNumber={}", orderNumber);
        return paymentService.cancelPayment(orderNumber);
    }
}
