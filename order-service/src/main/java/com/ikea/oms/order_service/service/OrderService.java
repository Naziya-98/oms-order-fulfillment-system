package com.ikea.oms.order_service.service;

import com.ikea.oms.order_service.dto.InventoryResponseDTO;
import com.ikea.oms.order_service.dto.OrderRequestDTO;
import com.ikea.oms.order_service.dto.OrderResponseDTO;
import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.event.OrderCreatedEvent;
import com.ikea.oms.order_service.exception.InsufficientInventoryException;
import com.ikea.oms.order_service.exception.InventoryNotFoundException;
import com.ikea.oms.order_service.kafka.OrderProducer;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final OrderProducer orderProducer;

    public OrderService(
            OrderRepository orderRepository,
            WebClient webClient,
            OrderProducer orderProducer) {

        this.orderRepository = orderRepository;
        this.webClient = webClient;
        this.orderProducer = orderProducer;
    }

    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        log.info("Order creation started. SKU={}, Quantity={}", request.getSkuCode(), request.getQuantity());

        Order order = new Order();

        order.setOrderNumber("ORD" + System.currentTimeMillis());
        order.setSkuCode(request.getSkuCode());
        order.setQuantity(request.getQuantity());

        log.info("Generated Order Number={}", order.getOrderNumber());

        InventoryResponseDTO inventoryResponse;

        try {

            log.info("Checking inventory for SKU={}", request.getSkuCode());

            inventoryResponse =
                    webClient.get()
                            .uri("/api/inventory/" + request.getSkuCode())
                            .retrieve()
                            .bodyToMono(InventoryResponseDTO.class)
                            .block();

        } catch (Exception e) {

            log.error("Inventory lookup failed for SKU={}", request.getSkuCode(), e);

            throw new InventoryNotFoundException(
                    "Inventory not found for SKU: " + request.getSkuCode()
            );
        }

        if (inventoryResponse == null) {

            log.error("Inventory response is null for SKU={}", request.getSkuCode());

            throw new InventoryNotFoundException("Inventory not found");
        }

        log.info("Inventory found. SKU={}, AvailableQuantity={}", inventoryResponse.getSkuCode(), inventoryResponse.getQuantity());

        log.info("Validating inventory availability");

        if (request.getQuantity() > inventoryResponse.getQuantity()) {

            log.error("Insufficient inventory. Requested={}, Available={}", request.getQuantity(), inventoryResponse.getQuantity());

            throw new InsufficientInventoryException(
                    "Insufficient Inventory Available"
            );
        }

        log.info("Updating inventory. SKU={}, OrderedQuantity={}", request.getSkuCode(), request.getQuantity());

        InventoryResponseDTO updatedInventory =
                webClient.patch()
                        .uri("/api/inventory/"
                                + request.getSkuCode()
                                + "/"
                                + request.getQuantity())
                        .retrieve()
                        .bodyToMono(InventoryResponseDTO.class)
                        .block();

        if (updatedInventory != null) {

            log.info("Inventory updated successfully. RemainingQuantity={}", updatedInventory.getQuantity());
        }

        log.info("Saving order into database. OrderNumber={}", order.getOrderNumber());

        Order savedOrder = orderRepository.save(order);

        log.info("Order saved successfully. OrderId={}, OrderNumber={}", savedOrder.getId(), savedOrder.getOrderNumber());

        log.info("Creating OrderCreatedEvent for OrderNumber={}", savedOrder.getOrderNumber());

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getSkuCode(),
                savedOrder.getQuantity()
        );

        orderProducer.publishOrderEvent(event);

        log.info("Kafka event published for OrderNumber={}", savedOrder.getOrderNumber());

        OrderResponseDTO response = new OrderResponseDTO();

        response.setId(savedOrder.getId());
        response.setOrderNumber(savedOrder.getOrderNumber());
        response.setSkuCode(savedOrder.getSkuCode());
        response.setQuantity(savedOrder.getQuantity());

        log.info("Order creation completed successfully. OrderNumber={}", savedOrder.getOrderNumber());

        return response;
    }

    public List<OrderResponseDTO> getAllOrders() {

        log.info("Fetching all orders from database");

        List<Order> orderList = orderRepository.findAll();

        log.info("Total orders found={}", orderList.size());

        return orderList.stream()
                .map(order -> {

                    OrderResponseDTO response =
                            new OrderResponseDTO();

                    response.setId(order.getId());
                    response.setOrderNumber(order.getOrderNumber());
                    response.setSkuCode(order.getSkuCode());
                    response.setQuantity(order.getQuantity());

                    return response;
                })
                .toList();
    }
}