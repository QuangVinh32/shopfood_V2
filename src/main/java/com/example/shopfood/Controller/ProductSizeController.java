package com.example.shopfood.Controller;

import com.example.shopfood.Model.DTO.ProductSizeResponse;
import com.example.shopfood.Model.Entity.ProductSize;
import com.example.shopfood.Model.Request.Product.ProductSizeRequest;
import com.example.shopfood.Service.IProductSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product_sizes")
@RequiredArgsConstructor
public class ProductSizeController {

    private final IProductSizeService productSizeService;

    // ================= GET =================

    // GET size theo product
    @GetMapping("/product/{productId}")
    public List<ProductSize> getByProduct(
            @PathVariable Integer productId
    ) {
        return productSizeService.getSizesByProductId(productId);
    }

    // GET size theo id
    @GetMapping("/{id}")
    public ProductSizeResponse get(@PathVariable Integer id) {
        ProductSize ps = productSizeService.getById(id);

        ProductSizeResponse res = new ProductSizeResponse();
        res.setProductSizeId(ps.getProductSizeId());
        res.setSizeName(String.valueOf(ps.getSizeName()));
        res.setPrice(ps.getPrice());
        res.setDiscount(ps.getDiscount());
        res.setQuantity(ps.getQuantity());

        return res;
    }

    // ================= CREATE =================

    @PostMapping("/product/{productId}")
    public ProductSize create(
            @PathVariable Integer productId,
            @RequestBody ProductSizeRequest request
    ) {
        return productSizeService.create(productId, request);
    }

    // ================= UPDATE =================

    @PutMapping("/{sizeId}")
    public ProductSize update(
            @PathVariable Integer sizeId,
            @RequestBody ProductSizeRequest request
    ) {
        return productSizeService.update(sizeId, request);
    }

    // ================= DELETE =================

    @DeleteMapping("/{sizeId}")
    public void delete(
            @PathVariable Integer sizeId
    ) {
        productSizeService.delete(sizeId);
    }

    // ================= BULK UPSERT =================

    @PostMapping("/bulk/{productId}")
    public void bulkUpsert(
            @PathVariable Integer productId,
            @RequestBody List<ProductSizeRequest> requests
    ) {
        productSizeService.bulkUpsert(productId, requests);
    }
}
