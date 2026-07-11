package com.ikea.oms.order_service.service;

import com.ikea.oms.order_service.dto.InventoryResponseDTO;
import com.ikea.oms.order_service.dto.OrderRequestDTO;
import com.ikea.oms.order_service.dto.OrderResponseDTO;
import com.ikea.oms.order_service.dto.ReserveInventoryRequestDTO;
import com.ikea.oms.order_service.dto.ReservationResponseDTO;
import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderItem;
import com.ikea.oms.order_service.entity.OrderStatus;
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

        log.info("Order creation started. SKU={}, Quantity={}",
                request.getSkuCode(),
                request.getQuantity());

        Order order = new Order();

        // Customer Details
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());

        order.setOrderNumber("ORD" + System.currentTimeMillis());
        order.setStatus(OrderStatus.CREATED);

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

            log.error("Inventory lookup failed for SKU={}",
                    request.getSkuCode(),
                    e);

            throw new InventoryNotFoundException(
                    "Inventory not found for SKU: " + request.getSkuCode());
        }

        if (inventoryResponse == null) {

            log.error("Inventory response is null for SKU={}",
                    request.getSkuCode());

            throw new InventoryNotFoundException("Inventory not found");
        }

        log.info(
                "Inventory found. SKU={}, ProductName={}, UnitPrice={}, AvailableQuantity={}",
                inventoryResponse.getSkuCode(),
                inventoryResponse.getProductName(),
                inventoryResponse.getUnitPrice(),
                inventoryResponse.getQuantity()
        );

        log.info("Validating inventory availability");

        if (request.getQuantity() > inventoryResponse.getQuantity()) {

            log.error(
                    "Insufficient inventory. Requested={}, Available={}",
                    request.getQuantity(),
                    inventoryResponse.getQuantity());

            throw new InsufficientInventoryException(
                    "Insufficient Inventory Available");
        }

        OrderItem orderItem = new OrderItem();

        orderItem.setSkuCode(inventoryResponse.getSkuCode());
        orderItem.setProductName(inventoryResponse.getProductName());
        orderItem.setUnitPrice(inventoryResponse.getUnitPrice());
        orderItem.setQuantity(request.getQuantity());

        orderItem.setOrder(order);

        order.getOrderItems().add(orderItem);

        log.info("Reserving inventory via reservation API. OrderNumber={}, SKU={}, OrderedQuantity={}",
                order.getOrderNumber(),
                request.getSkuCode(),
                request.getQuantity());

        ReserveInventoryRequestDTO reserveRequest = new ReserveInventoryRequestDTO();
        reserveRequest.setOrderNumber(order.getOrderNumber());
        reserveRequest.setSkuCode(request.getSkuCode());
        reserveRequest.setQuantity(request.getQuantity());

        ReservationResponseDTO reservation =
                webClient.post()
                        .uri("/api/inventory/reservations")
                        .bodyValue(reserveRequest)
                        .retrieve()
                        .bodyToMono(ReservationResponseDTO.class)
                        .block();

        order.setStatus(OrderStatus.INVENTORY_RESERVED);

        log.info(
                "Order {} status updated to INVENTORY_RESERVED",
                order.getOrderNumber());

        if (reservation != null) {

            log.info(
                    "Inventory reserved successfully. ReservationId={}",
                    reservation.getReservationId());
        }

        log.info("Saving order into database. OrderNumber={}",
                order.getOrderNumber());

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Order saved successfully. OrderId={}, OrderNumber={}",
                savedOrder.getId(),
                savedOrder.getOrderNumber());

        log.info("Creating OrderCreatedEvent for OrderNumber={}",
                savedOrder.getOrderNumber());

        OrderItem savedItem = savedOrder.getOrderItems().get(0);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getOrderNumber(),
                        null, // no saga correlation for direct order API
                        savedItem.getSkuCode(),
                        savedItem.getQuantity()
                );

        orderProducer.publishOrderEvent(event);

        log.info("Kafka event published for OrderNumber={}",
                savedOrder.getOrderNumber());

        OrderResponseDTO response = new OrderResponseDTO();

        response.setId(savedOrder.getId());
        response.setOrderNumber(savedOrder.getOrderNumber());
        response.setSkuCode(savedItem.getSkuCode());
        response.setQuantity(savedItem.getQuantity());

        // Customer Details
        response.setCustomerName(savedOrder.getCustomerName());
        response.setCustomerEmail(savedOrder.getCustomerEmail());
        response.setShippingAddress(savedOrder.getShippingAddress());

        log.info("Order creation completed successfully. OrderNumber={}",
                savedOrder.getOrderNumber());

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

                    OrderItem item = order.getOrderItems().get(0);

                    response.setSkuCode(item.getSkuCode());
                    response.setQuantity(item.getQuantity());

                    // Customer Details
                    response.setCustomerName(order.getCustomerName());
                    response.setCustomerEmail(order.getCustomerEmail());
                    response.setShippingAddress(order.getShippingAddress());

                    return response;
                })
                .toList();
    }
}