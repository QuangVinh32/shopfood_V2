package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.ProductSize;
import com.example.shopfood.Model.Request.Product.ProductSizeRequest;

import java.util.List;

public interface IProductSizeService {

    List<ProductSize> getSizesByProductId(Integer productId);

    ProductSize create(Integer productId, ProductSizeRequest request);

    ProductSize update(Integer sizeId, ProductSizeRequest request);

    void delete(Integer sizeId);

    ProductSize getById(Integer sizeId);

    boolean hasEnoughStock(Integer sizeId, int quantity);

    void changeStock(Integer sizeId, int delta);

    void bulkUpsert(Integer productId, List<ProductSizeRequest> sizes);
}
