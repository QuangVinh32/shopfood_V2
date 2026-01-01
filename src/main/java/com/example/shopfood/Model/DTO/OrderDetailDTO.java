package com.example.shopfood.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderDetailDTO {
    private Integer productId;
    private String productName;
    private Integer quantity;
    private Double price;
}
