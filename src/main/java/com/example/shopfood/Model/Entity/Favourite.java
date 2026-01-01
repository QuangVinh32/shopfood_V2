package com.example.shopfood.Model.Entity;

import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import jakarta.persistence.*;


@Entity
@Table(
        name = "favourites"
)
public class Favourite {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer favoriteId;
    @CreationTimestamp
    @Column(
            name = "create_at",
            nullable = false,
            updatable = false
    )
    private Date createdAt;
    @ManyToOne
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;
    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private Users user;
}