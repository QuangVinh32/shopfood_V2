package com.example.shopfood.Model.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_sizes")
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productSizeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "size_name", nullable = false)
    private ProductSizeEnum sizeName; // Enum: S, M, L...

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "discount", columnDefinition = "INT DEFAULT 0")
    private Integer discount;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 0; // Quản lý kho theo size
}
