package com.ikea.oms.orchestratorservice.controller;

import com.ikea.oms.orchestratorservice.dto.OrderRequestDTO;
import com.ikea.oms.orchestratorservice.dto.OrderResponseDTO;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orchestrated-orders")
@RequiredArgsConstructor
public class SagaOrderController {

    private final SagaOrchestratorService sagaOrchestratorService;

    @PostMapping
    public OrderResponseDTO createOrder(@Valid @RequestBody OrderRequestDTO request) {

        log.info("Received orchestrated order creation request. SKU={}, Quantity={}",
                request.getSkuCode(), request.getQuantity());

        return sagaOrchestratorService.startSaga(request);
    }

    @GetMapping
    public List<OrderResponseDTO> getAllOrders() {
        return sagaOrchestratorService.getAll();
    }

    // Poll this while the saga is running (in-progress -> completed / cancelled)
    @GetMapping("/{orderNumber}")
    public OrderResponseDTO getOrder(@PathVariable String orderNumber) {
        return sagaOrchestratorService.getByOrderNumber(orderNumber);
    }

    // Real cancel-order endpoint (replaces the FAIL_PAYMENT sku-code hack).
    @PostMapping("/{orderNumber}/cancel")
    public OrderResponseDTO cancelOrder(@PathVariable String orderNumber) {
        log.info("Received cancel-order request. OrderNumber={}", orderNumber);
        return sagaOrchestratorService.cancelOrder(orderNumber);
    }
}
