package com.example.shopfood.Model.Request.Voucher;

import com.example.shopfood.Model.Entity.DiscountType;
import com.example.shopfood.Model.Entity.VoucherStatus;
import com.example.shopfood.Model.Entity.VoucherTarget;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateVoucher {

    @NotBlank
    private String description;

    @NotNull
    private DiscountType discountType;

    @NotNull
    private Integer discountValue;

    private Integer maxDiscount;
    private Integer minOrderValue;

    private Integer usageLimitGlobal;
    private Integer usageLimitPerUser;

    @NotNull
    private VoucherTarget target;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date endDate;

    @NotNull
    private VoucherStatus status;

    // chỉ dùng khi target = USER
    private List<Integer> userIds;
}
