package com.example.shopfood.Model.Request.Order;

import lombok.Data;

@Data
public class FilterOrder {
    private String search;
    private Integer minId;
    private Integer maxId;
    private Integer minTotal;
    private Integer maxTotal;
    private Boolean idAsc;
    private Boolean idDesc;
    private Boolean totalAsc;
    private Boolean totalDesc;
}
