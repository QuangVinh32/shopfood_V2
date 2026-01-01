package com.example.shopfood.Model.Entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "reviews"
)
public class Review {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer reviewId;
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
    @Column(
            name = "rating"
    )
    private Integer rating;
    @Column(
            name = "review_text"
    )
    private String reviewText;
    @CreationTimestamp
    @Column(
            name = "create_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}