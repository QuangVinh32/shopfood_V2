package com.example.shopfood.Model.Entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderDetailPK implements Serializable {
    private Integer order;
    private Integer product;

}
