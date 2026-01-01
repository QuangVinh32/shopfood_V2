package com.example.shopfood.Model.Entity;

import lombok.Data;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "user_voucher")
@Data
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Temporal(TemporalType.TIMESTAMP)
    private Date assignedAt;
}
