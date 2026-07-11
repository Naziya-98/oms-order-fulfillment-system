package com.ikea.oms.inventoryservice.service;

import com.ikea.oms.inventoryservice.dto.InventoryRequestDTO;
import com.ikea.oms.inventoryservice.dto.InventoryResponseDTO;
import com.ikea.oms.inventoryservice.dto.ReservationResponseDTO;
import com.ikea.oms.inventoryservice.dto.ReserveInventoryRequestDTO;
import com.ikea.oms.inventoryservice.entity.Inventory;
import com.ikea.oms.inventoryservice.entity.Reservation;
import com.ikea.oms.inventoryservice.entity.ReservationStatus;
import com.ikea.oms.inventoryservice.exception.InsufficientInventoryException;
import com.ikea.oms.inventoryservice.exception.InventoryNotFoundException;
import com.ikea.oms.inventoryservice.exception.ReservationNotFoundException;
import com.ikea.oms.inventoryservice.repository.InventoryRepository;
import com.ikea.oms.inventoryservice.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(InventoryRepository inventoryRepository, ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    public InventoryResponseDTO saveInventory(InventoryRequestDTO request) {

        Inventory inventory = new Inventory();

        inventory.setSkuCode(request.getSkuCode());
        inventory.setProductName(request.getProductName());
        inventory.setCategory(request.getCategory());
        inventory.setUnitPrice(request.getUnitPrice());
        inventory.setQuantity(request.getQuantity());

        log.info(
                "Saving inventory. SKU={}, Product={}, Quantity={}",
                request.getSkuCode(),
                request.getProductName(),
                request.getQuantity()
        );

        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info(
                "Inventory saved successfully with Id={}",
                savedInventory.getId()
        );

        InventoryResponseDTO response = new InventoryResponseDTO();

        response.setId(savedInventory.getId());
        response.setSkuCode(savedInventory.getSkuCode());
        response.setProductName(savedInventory.getProductName());
        response.setCategory(savedInventory.getCategory());
        response.setUnitPrice(savedInventory.getUnitPrice());
        response.setQuantity(savedInventory.getQuantity());

        return response;
    }

    public List<InventoryResponseDTO> getAllInventory() {

        log.info("Fetching all inventory records from database");

        List<Inventory> inventoryList = inventoryRepository.findAll();

        log.info("Total inventory records found={}", inventoryList.size());

        return inventoryList.stream()
                .map(inventory -> {

                    InventoryResponseDTO response = new InventoryResponseDTO();

                    response.setId(inventory.getId());
                    response.setSkuCode(inventory.getSkuCode());
                    response.setProductName(inventory.getProductName());
                    response.setCategory(inventory.getCategory());
                    response.setUnitPrice(inventory.getUnitPrice());
                    response.setQuantity(inventory.getQuantity());

                    return response;
                })
                .toList();
    }

    public InventoryResponseDTO getInventoryBySkuCode(String skuCode) {

        log.info("Searching inventory for SKU={}", skuCode);

        Inventory inventory = inventoryRepository
                .findBySkuCode(skuCode)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for SKU: " + skuCode));

        log.info(
                "Inventory found. SKU={}, AvailableQuantity={}",
                inventory.getSkuCode(),
                inventory.getQuantity()
        );

        InventoryResponseDTO response = new InventoryResponseDTO();

        response.setId(inventory.getId());
        response.setSkuCode(inventory.getSkuCode());
        response.setProductName(inventory.getProductName());
        response.setCategory(inventory.getCategory());
        response.setUnitPrice(inventory.getUnitPrice());
        response.setQuantity(inventory.getQuantity());

        return response;
    }

    public InventoryResponseDTO updateInventory(String skuCode, Integer orderedQuantity) {

        log.info("Inventory update started. SKU={}, OrderedQuantity={}",
                skuCode,
                orderedQuantity);

        Inventory inventory = inventoryRepository
                .findBySkuCode(skuCode)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for SKU: " + skuCode));

        log.info("Current available quantity for SKU {} is {}",
                skuCode,
                inventory.getQuantity());

        inventory.setQuantity(
                inventory.getQuantity() - orderedQuantity);

        Inventory updatedInventory =
                inventoryRepository.save(inventory);

        log.info(
                "Inventory updated successfully. SKU={}, RemainingQuantity={}",
                updatedInventory.getSkuCode(),
                updatedInventory.getQuantity());

        InventoryResponseDTO response =
                new InventoryResponseDTO();

        response.setId(updatedInventory.getId());
        response.setSkuCode(updatedInventory.getSkuCode());
        response.setProductName(updatedInventory.getProductName());
        response.setCategory(updatedInventory.getCategory());
        response.setUnitPrice(updatedInventory.getUnitPrice());
        response.setQuantity(updatedInventory.getQuantity());

        return response;
    }
    public InventoryResponseDTO releaseInventory(String skuCode, Integer releasedQuantity) {

        log.info(
                "Inventory compensation started. SKU={}, ReleasedQuantity={}",
                skuCode,
                releasedQuantity
        );

        Inventory inventory = inventoryRepository
                .findBySkuCode(skuCode)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for SKU: " + skuCode));

        log.info(
                "Current available quantity before release for SKU {} is {}",
                skuCode,
                inventory.getQuantity()
        );

        inventory.setQuantity(
                inventory.getQuantity() + releasedQuantity
        );

        Inventory updatedInventory = inventoryRepository.save(inventory);

        log.info(
                "Inventory released successfully. SKU={}, AvailableQuantity={}",
                updatedInventory.getSkuCode(),
                updatedInventory.getQuantity()
        );

        InventoryResponseDTO response = new InventoryResponseDTO();

        response.setId(updatedInventory.getId());
        response.setSkuCode(updatedInventory.getSkuCode());
        response.setProductName(updatedInventory.getProductName());
        response.setCategory(updatedInventory.getCategory());
        response.setUnitPrice(updatedInventory.getUnitPrice());
        response.setQuantity(updatedInventory.getQuantity());

        return response;
    }

    public InventoryResponseDTO updateInventoryStock(
            String skuCode,
            Integer quantity) {

        log.info(
                "Manual stock update started. SKU={}, NewQuantity={}",
                skuCode,
                quantity);

        Inventory inventory = inventoryRepository
                .findBySkuCode(skuCode)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for SKU: " + skuCode));

        log.info(
                "Current quantity for SKU {} is {}",
                skuCode,
                inventory.getQuantity());

        inventory.setQuantity(quantity);

        Inventory updatedInventory =
                inventoryRepository.save(inventory);

        log.info(
                "Stock updated successfully. SKU={}, UpdatedQuantity={}",
                updatedInventory.getSkuCode(),
                updatedInventory.getQuantity());

        InventoryResponseDTO response =
                new InventoryResponseDTO();

        response.setId(updatedInventory.getId());
        response.setSkuCode(updatedInventory.getSkuCode());
        response.setProductName(updatedInventory.getProductName());
        response.setCategory(updatedInventory.getCategory());
        response.setUnitPrice(updatedInventory.getUnitPrice());
        response.setQuantity(updatedInventory.getQuantity());

        return response;
    }

    // ─── Reservation-based reserve/release (additive, new API) ────────
    // These do not replace updateInventory/releaseInventory above, which
    // the existing Kafka choreography/orchestration consumers still call
    // exactly as before. This is a parallel, explicit REST-friendly path.

    @Transactional
    public ReservationResponseDTO reserve(ReserveInventoryRequestDTO request) {

        Inventory inventory = inventoryRepository
                .findBySkuCode(request.getSkuCode())
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for SKU: " + request.getSkuCode()));

        if (inventory.getQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException(
                    "Insufficient stock for SKU " + request.getSkuCode()
                            + ". Available=" + inventory.getQuantity()
                            + ", Requested=" + request.getQuantity());
        }

        inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
        inventoryRepository.save(inventory);

        Reservation reservation = new Reservation();
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setOrderNumber(request.getOrderNumber());
        reservation.setSkuCode(request.getSkuCode());
        reservation.setQuantity(request.getQuantity());
        reservation.setStatus(ReservationStatus.ACTIVE);

        Reservation saved = reservationRepository.save(reservation);

        log.info(
                "Inventory reserved. ReservationId={}, OrderNumber={}, SKU={}, Quantity={}",
                saved.getReservationId(), saved.getOrderNumber(), saved.getSkuCode(), saved.getQuantity());

        return new ReservationResponseDTO(
                saved.getReservationId(),
                saved.getOrderNumber(),
                saved.getSkuCode(),
                saved.getQuantity(),
                "RESERVED",
                "Inventory reserved successfully");
    }

    @Transactional
    public ReservationResponseDTO release(String reservationId) {

        Reservation reservation = reservationRepository
                .findByReservationId(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                "Reservation not found: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            log.warn("Reservation {} already released. Ignoring duplicate release request.", reservationId);
            return new ReservationResponseDTO(
                    reservation.getReservationId(),
                    reservation.getOrderNumber(),
                    reservation.getSkuCode(),
                    reservation.getQuantity(),
                    "RELEASED",
                    "Reservation was already released (idempotent no-op)");
        }

        Inventory inventory = inventoryRepository
                .findBySkuCode(reservation.getSkuCode())
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for SKU: " + reservation.getSkuCode()));

        inventory.setQuantity(inventory.getQuantity() + reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        log.info(
                "Reservation released (compensation). ReservationId={}, SKU={}, Quantity={}",
                reservationId, reservation.getSkuCode(), reservation.getQuantity());

        return new ReservationResponseDTO(
                reservation.getReservationId(),
                reservation.getOrderNumber(),
                reservation.getSkuCode(),
                reservation.getQuantity(),
                "RELEASED",
                "Inventory released successfully");
    }

    // ─── Order-facing convenience wrappers used by the actual saga flows ──
    // (both choreography's order-service and orchestration's
    // SagaReserve/ReleaseInventoryCommandConsumer call these). They wrap the
    // reserve()/release() pair above so the automated flow now uses the same
    // tracked Reservation model as the manual /api/inventory/reservations
    // endpoints, instead of the old blind quantity math.

    @Transactional
    public ReservationResponseDTO reserveForOrder(String orderNumber, String skuCode, Integer quantity) {

        ReserveInventoryRequestDTO request = new ReserveInventoryRequestDTO();
        request.setOrderNumber(orderNumber);
        request.setSkuCode(skuCode);
        request.setQuantity(quantity);

        return reserve(request);
    }

    @Transactional
    public ReservationResponseDTO releaseForOrder(String orderNumber) {

        return reservationRepository.findByOrderNumberAndStatus(orderNumber, ReservationStatus.ACTIVE)
                .map(reservation -> release(reservation.getReservationId()))
                .orElseGet(() -> {
                    log.warn("No ACTIVE reservation found for OrderNumber={}. Nothing to release (idempotent no-op).", orderNumber);
                    return new ReservationResponseDTO(null, orderNumber, null, null, "NOT_FOUND",
                            "No active reservation found for this order");
                });
    }
}