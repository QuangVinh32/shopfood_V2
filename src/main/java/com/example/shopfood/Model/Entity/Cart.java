package com.example.shopfood.Model.Entity;

import lombok.Data;

import jakarta.persistence.*;

import java.util.List;

@Data
@Entity
@Table(
        name = "carts"
)
public class Cart {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer cartId;
    @OneToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private Users user;
    @OneToMany(
            mappedBy = "cart",
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    private List<CartDetail> cartDetails;
}
