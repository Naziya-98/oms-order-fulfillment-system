package com.ikea.oms.inventoryservice.service;

import com.ikea.oms.inventoryservice.dto.InventoryRequestDTO;
import com.ikea.oms.inventoryservice.dto.InventoryResponseDTO;
import com.ikea.oms.inventoryservice.entity.Inventory;
import com.ikea.oms.inventoryservice.exception.InventoryNotFoundException;
import com.ikea.oms.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
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
    }}