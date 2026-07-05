package com.ikea.oms.orchestratorservice.repository;

import com.ikea.oms.orchestratorservice.entity.SagaOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SagaOrderRepository extends JpaRepository<SagaOrder, Long> {

    Optional<SagaOrder> findByOrderNumber(String orderNumber);
}
