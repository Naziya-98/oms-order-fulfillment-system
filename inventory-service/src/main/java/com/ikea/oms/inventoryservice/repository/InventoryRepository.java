package com.ikea.oms.inventoryservice.repository;
import com.ikea.oms.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import javax.crypto.spec.IvParameterSpec;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory , Long>{
    Optional<Inventory> findBySkuCode(String skuCode);
}
