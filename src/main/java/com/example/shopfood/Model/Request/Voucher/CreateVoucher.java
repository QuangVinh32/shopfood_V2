package com.example.shopfood.Model.Request.Voucher;

import com.example.shopfood.Model.Entity.DiscountType;
import com.example.shopfood.Model.Entity.VoucherStatus;
import com.example.shopfood.Model.Entity.VoucherTarget;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Data
public class CreateVoucher {

    // ===== THÔNG TIN CƠ BẢN =====
    @NotBlank
    private String code;


    private String description;

    // ===== KIỂU GIẢM GIÁ =====
    @NotNull
    private DiscountType discountType;
    // FIXED | PERCENT

    @NotNull
    private Integer discountValue;

    // Giảm tối đa (chỉ dùng cho PERCENT)
    private Integer maxDiscount;

    // ===== ĐIỀU KIỆN ĐƠN HÀNG =====
    private Integer minOrderValue;

    // ===== GIỚI HẠN SỬ DỤNG =====
    private Integer usageLimitGlobal;   // tổng lượt dùng
    private Integer usageLimitPerUser;  // mỗi user

    // ===== ĐỐI TƯỢNG ÁP DỤNG =====
    @NotNull
    private VoucherTarget target;
    // PUBLIC | USER

    // ===== THỜI GIAN =====
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")

    private Date startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date endDate;

    // ===== USER RIÊNG =====
    private List<Integer> userIds;
}

