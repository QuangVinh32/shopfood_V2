package com.example.shopfood.Model.Entity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "payments"
)
public class Payment {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer paymentId;
    @Column(
            name = "code"
    )
    private String code;
}