package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.OrderDetail;
import com.example.shopfood.Model.Entity.OrderDetailPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailPK> {
    List<OrderDetail> findByOrderOrderId(int id);
}