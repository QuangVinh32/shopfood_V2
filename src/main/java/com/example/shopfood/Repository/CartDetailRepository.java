package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Cart;
import com.example.shopfood.Model.Entity.CartDetail;
import com.example.shopfood.Model.Entity.CartDetailPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, CartDetailPK> {
    List<CartDetail> findAllByCart(Cart cart);

    List<CartDetail> findByCart(Cart cart);
}