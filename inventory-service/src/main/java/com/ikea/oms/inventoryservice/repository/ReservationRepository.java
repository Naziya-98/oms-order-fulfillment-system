package com.ikea.oms.inventoryservice.repository;

import com.ikea.oms.inventoryservice.entity.Reservation;
import com.ikea.oms.inventoryservice.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationId(String reservationId);
    Optional<Reservation> findByOrderNumberAndStatus(String orderNumber, ReservationStatus status);
}
