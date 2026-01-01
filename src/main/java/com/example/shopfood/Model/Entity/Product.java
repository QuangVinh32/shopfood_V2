package com.example.shopfood.Model.Entity;

import lombok.Data;

import java.util.List;
import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "products"
)
public class Product {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer productId;
    @Column(
            name = "product_name",
            nullable = false,
            length = 255
    )
    private String productName;
//    @Column(
//            name = "product_image"
//    )
//    private String productImage;
    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;
    @Column(
            name = "price",
            nullable = false
    )
    private Double price;
    @Column(
            name = "discount",
            columnDefinition = "INT DEFAULT 0"
    )
    private Integer discount;
    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;
    @ManyToOne
    @JoinColumn(
            name = "category_id",
            nullable = false
    )
    private Category category;
    @OneToMany(
            mappedBy = "product",
            cascade = {CascadeType.ALL}
    )
    private List<Review> reviews;

    @OneToMany(
            mappedBy = "product",
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<ProductImage> productImages;
}