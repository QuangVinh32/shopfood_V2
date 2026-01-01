package com.example.shopfood.Model.Entity;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;


import java.util.Date;
import java.util.List;
@Data
@Entity
@Table(
        name = "orders"
)
public class Order {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer orderId;
    @Column(
            name = "total_amount"
    )
    private Integer totalAmount;
    @Enumerated(EnumType.STRING)
    @Column(
            name = "order_status",
            nullable = false,
            length = 15
    )
    private OrderStatus orderStatus;
    @CreationTimestamp
    @Column(
            name = "create_at",
            nullable = false,
            updatable = false
    )
    private Date createdAt;
    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private Users user;
    @ManyToOne
    @JoinColumn(
            name = "payment_id"
    )
    private Payment payment;
    @OneToOne
    @JoinColumn(
            name = "voucher_id"
    )
    private Voucher voucher;
    @OneToMany(
            mappedBy = "order",
            cascade = {CascadeType.ALL}
    )
    private List<OrderDetail> orderDetails;
}
