package com.example.shopfood.Model.Request.Voucher;

import com.example.shopfood.Model.Entity.VoucherStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.List;

@Data
public class CreateVoucher {

    private String code;

    private String description;

    private Integer usageLimit;

    private VoucherStatus status;

    private Integer discountValue;

    private List<Integer> userIds; // thêm để gán user

}
