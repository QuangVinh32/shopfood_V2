package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDTO {
    private Integer orderId;
    private Integer totalAmount;
    private OrderStatus status;
    private Date createdAt;
    private Integer userId;
    private List<OrderDetailDTO> orderDetails;
}
