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

}
