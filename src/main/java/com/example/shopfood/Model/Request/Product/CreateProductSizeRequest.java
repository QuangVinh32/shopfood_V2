package com.example.shopfood.Model.Request.Product;


import com.example.shopfood.Model.Entity.ProductSizeEnum;
import lombok.Data;

@Data
public class CreateProductSizeRequest {
    private ProductSizeEnum sizeName;
    private Double price;
    private Integer discount;
    private Integer quantity;
}