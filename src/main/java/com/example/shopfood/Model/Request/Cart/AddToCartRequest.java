package com.example.shopfood.Model.Request.Cart;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Integer productId;
    private Integer productSizeId;
    private Integer quantity;
}
