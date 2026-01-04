package com.example.shopfood.Model.DTO;

import lombok.Data;

@Data
public class OrderDetailDTO {
    private String productName;
    private Integer productSizeId; // Thêm
    private String sizeName; // Thêm
    private Integer quantity;
    private Double price;
    private Integer discountApplied; // Thêm nếu cần

    // Constructors
    public OrderDetailDTO() {}

    public OrderDetailDTO(String productName, Integer quantity, Double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderDetailDTO(String productName, Integer productSizeId, String sizeName,
                          Integer quantity, Double price) {
        this.productName = productName;
        this.productSizeId = productSizeId;
        this.sizeName = sizeName;
        this.quantity = quantity;
        this.price = price;
    }
}