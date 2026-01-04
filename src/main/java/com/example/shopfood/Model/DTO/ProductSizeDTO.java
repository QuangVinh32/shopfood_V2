package com.example.shopfood.Model.DTO;

import lombok.Data;

@Data
public class ProductSizeDTO {
    private String sizeName;
    private Double price;
    private Integer discount;
    private Integer quantity;

    // Constructor đầy đủ
    public ProductSizeDTO(String sizeName, Double price, Integer discount, Integer quantity) {
        this.sizeName = sizeName;
        this.price = price;
        this.discount = discount;
        this.quantity = quantity;
    }

    // Nếu muốn, vẫn giữ constructor mặc định
    public ProductSizeDTO() {
    }
}
