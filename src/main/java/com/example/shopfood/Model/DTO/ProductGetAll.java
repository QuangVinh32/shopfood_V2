package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.CategoryStatus;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductGetAll {
    private String productName;
    private String description;
    private List<String> productImages = new ArrayList<>();
    private Integer categoryId;
    private String categoryImage;
    private CategoryStatus categoryStatus;

}
