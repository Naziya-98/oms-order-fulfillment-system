package com.ikea.oms.inventoryservice.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name="inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private Integer quantity;
}
