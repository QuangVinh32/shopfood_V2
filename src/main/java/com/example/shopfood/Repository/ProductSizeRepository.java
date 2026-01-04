package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSizeRepository  extends JpaRepository<ProductSize, Integer> {
}
