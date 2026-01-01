package com.example.shopfood.Model.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDetailPK implements Serializable {
    private Integer cart;
    private Integer product;
}
