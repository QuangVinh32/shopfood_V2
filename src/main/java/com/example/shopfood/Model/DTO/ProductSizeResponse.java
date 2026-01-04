package com.example.shopfood.Model.DTO;

import lombok.Data;

@Data
public class ProductSizeResponse {
    private Integer productSizeId;
    private String sizeName;
    private Double price;
    private Integer discount;
    private Integer quantity;
}
