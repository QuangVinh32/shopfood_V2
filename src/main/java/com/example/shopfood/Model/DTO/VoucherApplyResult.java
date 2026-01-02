package com.example.shopfood.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoucherApplyResult {
    private Integer originalAmount;
    private Integer discountAmount;
    private Integer finalAmount;
}
