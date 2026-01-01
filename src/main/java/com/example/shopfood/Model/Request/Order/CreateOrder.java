package com.example.shopfood.Model.Request.Order;

import com.example.shopfood.Model.Entity.OrderStatus;
import lombok.Data;

import java.util.Date;
@Data
public class CreateOrder {
    private Integer totalAmount;
    private OrderStatus status;
    private Date createdAt;
}
