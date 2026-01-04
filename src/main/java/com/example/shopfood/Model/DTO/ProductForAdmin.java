package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.CategoryStatus;
import lombok.Data;

import java.util.List;

@Data
public class ProductForAdmin {
    private Integer productId;
    private String productName;
    private List<String> productImages;
    private String description;
//    private Double price;
//    private Integer discount;
//    private Integer quantity;
//    private Integer categoryId;
    private CategoryStatus categoryStatus;
    private List<ProductSizeDTO> sizes;
}
