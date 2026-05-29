package com.example.shopfood.Model.Request.Shipping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShippingAddressRequest {

    @NotBlank
    private String receiverName;

    @NotBlank
    @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$", message = "Số điện thoại không hợp lệ")
    private String receiverPhone;

    @NotBlank
    private String addressLine;

    private String ward;
    private String district;
    private String province;

    private boolean isDefault;
}
