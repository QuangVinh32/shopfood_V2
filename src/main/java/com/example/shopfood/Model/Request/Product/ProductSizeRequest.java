package com.example.shopfood.Model.Request.Product;

import lombok.Data;

@Data
public class ProductSizeRequest {
    private String sizeName; // enum
    private Double price;
    private Integer discount;
    private Integer quantity;
}
