package com.example.shopfood.Service;

import com.example.shopfood.Model.DTO.ProductForAdmin;
import com.example.shopfood.Model.DTO.ProductForUser;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Request.Product.CreateProduct;
import com.example.shopfood.Model.Request.Product.FilterProduct;
import com.example.shopfood.Model.Request.Product.UpdateProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface IProductService {
    Page<Product> getAllProductsPage(Pageable pageable, FilterProduct filterProduct);

    ProductForAdmin getProductByIdForAdmin(int id);

    void createProduct(CreateProduct createProduct) throws Exception;

    void updateProduct(int productId, UpdateProduct updateProduct) throws Exception;

    void deleteProduct(int id);

    boolean isProductNameExists(String productName);

    ProductForUser getProductByIdForUser(int id);
}
