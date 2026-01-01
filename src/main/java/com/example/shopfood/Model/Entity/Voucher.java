package com.example.shopfood.Model.Entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;


@Data
@Entity
@Table(
        name = "vouchers"
)
public class Voucher {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer voucherId;
    @Column(
            name = "code"
    )
    private String code;
    @Column(
            name = "description"
    )
    private String description;
    @CreationTimestamp
    @Column(
            name = "start_date",
            nullable = false,
            updatable = false
    )
    private Date startDate;
    @Column(
            name = "end_date"
    )
    private Date endDate;
    @Column(
            name = "usage_limit"
    )
    private Integer usageLimit;
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 15
    )
    private VoucherStatus status;
    @Column(
            name = "discount_value"
    )
    private Integer discountValue;
}