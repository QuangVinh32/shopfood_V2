package com.example.shopfood.Service;

import com.example.shopfood.Model.DTO.OrderDTO;
import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Request.Order.FilterOrder;
import com.example.shopfood.Model.Request.Order.UpdateOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public interface IOrderService {

    Page<Order> getAllOrdersPage(Pageable pageable, FilterOrder filterOrder);

    OrderDTO getOrderById(int id);

    void createOrder(String voucherCode) throws Exception;

    OrderDTO updateOrder(int orderID, UpdateOrder updateOrder) throws Exception;

    void deleteOrder(int id);
}
