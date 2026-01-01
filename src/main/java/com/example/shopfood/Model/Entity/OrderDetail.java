package com.example.shopfood.Model.Entity;
import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "order_detail"
)
@IdClass(OrderDetailPK.class)
public class OrderDetail {
    @Id
    @ManyToOne
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;
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
    @Column(
            nullable = false
    )
    private Double price;
}
