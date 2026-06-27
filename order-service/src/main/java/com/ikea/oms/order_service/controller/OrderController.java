package com.ikea.oms.order_service.controller;
import com.ikea.oms.order_service.dto.OrderRequestDTO;
import com.ikea.oms.order_service.dto.OrderResponseDTO;
import com.ikea.oms.order_service.service.OrderService;
import java.util.List;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponseDTO createOrder(@Valid @RequestBody OrderRequestDTO request){

        log.info("Received order creation request. SKU={}, Quantity={}", request.getSkuCode(), request.getQuantity());
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<OrderResponseDTO> getAllOrders() {
        log.info("Fetching all orders");
        return orderService.getAllOrders();
    }
}
