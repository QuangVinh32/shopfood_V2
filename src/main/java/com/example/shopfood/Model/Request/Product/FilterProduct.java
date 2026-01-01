package com.example.shopfood.Model.Request.Product;

import lombok.Data;

@Data
public class FilterProduct {
    private String search;
    private Integer priceMin;
    private Integer priceMax;
    private Boolean priceAsc;
    private Boolean priceDesc;
    private Integer categoryId;
}
