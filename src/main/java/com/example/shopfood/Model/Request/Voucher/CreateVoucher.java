package com.example.shopfood.Model.Request.Voucher;

import com.example.shopfood.Model.Entity.DiscountType;
import com.example.shopfood.Model.Entity.VoucherTarget;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CreateVoucher {

    @NotBlank
    @Size(max = 50)
    private String code;

    @Size(max = 255)
    private String description;

    @NotNull
    private DiscountType discountType;
    // FIXED | PERCENT

    @NotNull
    @Min(value = 1, message = "discountValue phải >= 1")
    private Integer discountValue;

    @PositiveOrZero
    private Integer maxDiscount;

    @PositiveOrZero
    private Integer minOrderValue;

    @PositiveOrZero
    private Integer usageLimitGlobal;

    @PositiveOrZero
    private Integer usageLimitPerUser;

    @NotNull
    private VoucherTarget target;
    // ALL | USER

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date endDate;

    private List<Integer> userIds;

    @AssertTrue(message = "PERCENT voucher: discountValue phải <= 100")
    public boolean isPercentValid() {
        if (discountType == DiscountType.PERCENT && discountValue != null) {
            return discountValue <= 100;
        }
        return true;
    }

    @AssertTrue(message = "endDate phải sau startDate")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return endDate.after(startDate);
    }
}
