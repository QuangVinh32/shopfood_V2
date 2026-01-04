package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, CartDetailPK> {
    List<CartDetail> findAllByCart(Cart cart);

    List<CartDetail> findByCart(Cart cart);

    Optional<CartDetail> findByCartAndProductAndProductSize(Cart cart, Product product, ProductSize productSize);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cart_detail WHERE cart_id = :cartId", nativeQuery = true)
    void deleteByCartIdNative(@Param("cartId") Integer cartId);

}