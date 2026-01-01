package com.example.shopfood.Model.Request.Order;

import com.example.shopfood.Model.Entity.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrder {
    private OrderStatus status;
}
