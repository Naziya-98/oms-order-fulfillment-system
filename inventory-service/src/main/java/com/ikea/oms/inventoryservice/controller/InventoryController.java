package com.ikea.oms.inventoryservice.controller;
import com.ikea.oms.inventoryservice.dto.InventoryRequestDTO;
import com.ikea.oms.inventoryservice.dto.InventoryResponseDTO;
import com.ikea.oms.inventoryservice.entity.Inventory;
import com.ikea.oms.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryservice){
        this.inventoryService=inventoryservice;
    }

    @PostMapping
    public InventoryResponseDTO saveInventory(@Valid @RequestBody InventoryRequestDTO request){
        log.info("Received inventory creation request for SKU {}", request.getSkuCode());

        return inventoryService.saveInventory(request);
    }

    // ─── Reservation-based reserve/release (new, additive) ─────────────

    @PostMapping("/reservations")
    public com.ikea.oms.inventoryservice.dto.ReservationResponseDTO reserve(
            @jakarta.validation.Valid @RequestBody com.ikea.oms.inventoryservice.dto.ReserveInventoryRequestDTO request) {

        log.info(
                "Reservation requested. OrderNumber={}, SKU={}, Quantity={}",
                request.getOrderNumber(), request.getSkuCode(), request.getQuantity());

        return inventoryService.reserve(request);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public com.ikea.oms.inventoryservice.dto.ReservationResponseDTO release(
            @PathVariable String reservationId) {

        log.info("Reservation release (compensation) requested. ReservationId={}", reservationId);

        return inventoryService.release(reservationId);
    }

    @GetMapping
    public List<InventoryResponseDTO> getALLInventory(){
        log.info("Fetching all inventory records");
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{skuCode}")
    public InventoryResponseDTO getInventoryBySkuCode(@PathVariable String skuCode ) {

        log.info("Fetching inventory for SKU {}", skuCode);
        return inventoryService.getInventoryBySkuCode(skuCode);
    }

    @PatchMapping("/{skuCode}/{quantity}")
    public InventoryResponseDTO updateInventory(@PathVariable String skuCode, @PathVariable Integer quantity) {

        log.info("Inventory update request received. SKU={}, Quantity={}", skuCode, quantity);

        return inventoryService.updateInventory(
                skuCode,
                quantity);
    }

    // Manual compensating action - release previously reserved stock directly
    // via Postman (e.g. after cancelling a payment) instead of only reacting
    // to Kafka events.
    @PatchMapping("/{skuCode}/release/{quantity}")
    public InventoryResponseDTO releaseInventoryManually(
            @PathVariable String skuCode,
            @PathVariable Integer quantity) {

        log.info(
                "Manual inventory release (compensation) requested. SKU={}, Quantity={}",
                skuCode,
                quantity);

        return inventoryService.releaseInventory(skuCode, quantity);
    }

    @PatchMapping("/stock/{skuCode}/{quantity}")
    public InventoryResponseDTO updateInventoryStock(
            @PathVariable String skuCode,
            @PathVariable Integer quantity) {

        log.info(
                "Manual stock update request received. SKU={}, NewQuantity={}",
                skuCode,
                quantity);

        return inventoryService.updateInventoryStock(
                skuCode,
                quantity);
    }

}
