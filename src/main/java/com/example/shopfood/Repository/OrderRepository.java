package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Entity.OrderStatus;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    boolean existsByUserAndOrderDetailsProductAndOrderStatus(
            Users user, Product product, OrderStatus orderStatus
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = :status")
    Long sumTotalAmountByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.originalAmount), 0) FROM Order o WHERE o.orderStatus = :status")
    Long sumOriginalAmountByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.discountAmount), 0) FROM Order o WHERE o.orderStatus = :status")
    Long sumDiscountAmountByStatus(@Param("status") OrderStatus status);

}
