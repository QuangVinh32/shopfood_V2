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
            name = "original_amount"
    )
    private Integer originalAmount;
    @Column(
            name = "discount_amount"
    )
    private Integer discountAmount;
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
    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "shipping_fee", nullable = false)
    private Integer shippingFee = 0;

    @Column(name = "note", length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @OneToMany(
            mappedBy = "order",
            cascade = {CascadeType.ALL}
    )
    private List<OrderDetail> orderDetails;
}
