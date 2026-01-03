package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.Review;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer>, JpaSpecificationExecutor<Review> {
    List<Review> findByProductProductId(int productId);
    boolean existsByUserAndProduct(Users user, Product product);

}
