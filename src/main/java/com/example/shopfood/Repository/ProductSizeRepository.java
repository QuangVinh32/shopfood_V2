package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.ProductSize;
import com.example.shopfood.Model.Entity.ProductSizeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {

    List<ProductSize> findByProduct_ProductId(Integer productId);

    Optional<ProductSize> findByProductAndSizeName(Product product, ProductSizeEnum sizeName);
}
