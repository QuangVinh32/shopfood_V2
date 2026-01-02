package com.example.shopfood.Model.Entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;


@Entity
@Table(name = "vouchers")
@Data
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Integer voucherId;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private Integer discountValue;

    private Integer maxDiscount; // dùng cho %

    private Integer minOrderValue;

    @Enumerated(EnumType.STRING)
    private VoucherScope scope = VoucherScope.ORDER;

    @Enumerated(EnumType.STRING)
    private VoucherTarget target = VoucherTarget.ALL;

    // Tổng số lượt dùng toàn hệ thống
    private Integer usageLimitGlobal;

    // Mỗi user dùng tối đa bao nhiêu lần
    private Integer usageLimitPerUser;

    private Integer usedCount = 0;

    @Enumerated(EnumType.STRING)
    private VoucherStatus status = VoucherStatus.DRAFT;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
