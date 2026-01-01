package com.example.shopfood.Model.Entity;
import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "cart_detail"
)
@IdClass(CartDetailPK.class)
public class CartDetail {
    @Id
    @ManyToOne
    @JoinColumn(
            name = "cart_id",
            nullable = false
    )
    private Cart cart;
    @Id
    @ManyToOne
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;
    @Column(
            nullable = false
    )
    private Integer quantity;
}