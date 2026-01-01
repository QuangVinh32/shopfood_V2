package com.example.shopfood.Model.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "product_image")
public class ProductImage {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer productImageId;
    @Column(
            name = "product_image_name"
    )
    private String productImageName;
    @Column(
            name = "product_image_path"
    )
    private String productImagePath;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
