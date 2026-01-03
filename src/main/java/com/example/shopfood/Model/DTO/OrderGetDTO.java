package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.OrderStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderGetDTO {
    private Integer orderId;
    private Integer totalAmount;
    private OrderStatus status;
    private Date createdAt;

    // USER INFO (ADMIN CẦN)
//    private Integer userId;
    private String fullName;
    private String phone;
    private String address;

    private List<OrderDetailDTO> orderDetails;
}
