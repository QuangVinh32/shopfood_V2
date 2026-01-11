package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.ProductSize;
import com.example.shopfood.Model.Entity.ProductSizeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {

    List<ProductSize> findByProduct_ProductId(Integer productId);

    Optional<ProductSize> findByProductAndSizeName(Product product, ProductSizeEnum sizeName);

    @Modifying
    @Query("""
        UPDATE ProductSize ps
        SET ps.quantity = ps.quantity - :qty
        WHERE ps.productSizeId = :id
          AND ps.quantity >= :qty
    """)
    int decreaseStock(@Param("id") Integer productSizeId,
                      @Param("qty") int quantity);
}
