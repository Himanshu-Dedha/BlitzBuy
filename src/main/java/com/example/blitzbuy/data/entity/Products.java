package com.example.blitzbuy.data.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="products")
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(name = "product_seq_gen", sequenceName = "product_sequence", allocationSize = 1)
    @Column(name="id")
    private Long id;

    @Column(name="price")
    private Double price;

    @Column(name="inventory_count")
    private Long inventoryCount;
}
