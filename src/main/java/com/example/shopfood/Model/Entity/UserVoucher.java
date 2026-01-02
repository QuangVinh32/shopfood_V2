package com.example.shopfood.Model.Entity;

import lombok.Data;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "user_voucher",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "voucher_id"}))
@Data
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userVoucherId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    private Integer usedCount = 0;

    @Temporal(TemporalType.TIMESTAMP)
    private Date assignedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsedAt;
}

