package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Cart;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer>, JpaSpecificationExecutor<Cart> {
    List<Cart> findAllByUser(Users users);

    Optional<Cart> findByUser(Users user);
}