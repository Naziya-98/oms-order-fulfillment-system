package com.ikea.oms.order_service.repository;

import com.ikea.oms.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    // Used by the saga.* event consumers to correlate back to this row without
    // relying on the business orderNumber having been round-tripped from the
    // orchestrator yet (see Order.sagaOrderId for why).
    Optional<Order> findBySagaOrderId(Long sagaOrderId);
}