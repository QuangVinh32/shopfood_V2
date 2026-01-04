package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.ProductSize;
import com.example.shopfood.Model.Request.Product.ProductSizeRequest;
import com.example.shopfood.Repository.ProductRepository;
import com.example.shopfood.Repository.ProductSizeRepository;
import com.example.shopfood.Service.IProductSizeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductSizeService implements IProductSizeService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    // ================= GET =================

    @Override
    public List<ProductSize> getSizesByProductId(Integer productId) {
        return productSizeRepository.findByProduct_ProductId(productId);
    }

    @Override
    public ProductSize getById(Integer sizeId) {
        return productSizeRepository.findById(sizeId)
            .orElseThrow(() -> new RuntimeException("ProductSize not found"));
    }

    // ================= CREATE =================

    @Override
    public ProductSize create(Integer productId, ProductSizeRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check trùng size
        productSizeRepository.findByProductAndSizeName(product, request.getSizeName())
            .ifPresent(ps -> {
                throw new RuntimeException("Size already exists for this product");
            });

        ProductSize size = new ProductSize();
        size.setProduct(product);
        size.setSizeName(request.getSizeName());
        size.setPrice(request.getPrice());
        size.setDiscount(
            request.getDiscount() == null ? 0 : request.getDiscount()
        );
        size.setQuantity(
            request.getQuantity() == null ? 0 : request.getQuantity()
        );

        return productSizeRepository.save(size);
    }

    // ================= UPDATE =================

    @Override
    public ProductSize update(Integer sizeId, ProductSizeRequest request) {
        ProductSize size = getById(sizeId);

        if (request.getSizeName() != null) {
            size.setSizeName(request.getSizeName());
        }
        if (request.getPrice() != null) {
            size.setPrice(request.getPrice());
        }
        if (request.getDiscount() != null) {
            size.setDiscount(request.getDiscount());
        }
        if (request.getQuantity() != null) {
            size.setQuantity(request.getQuantity());
        }

        return productSizeRepository.save(size);
    }

    // ================= DELETE =================

    @Override
    public void delete(Integer sizeId) {
        ProductSize size = getById(sizeId);
        productSizeRepository.delete(size);
    }

    // ================= STOCK =================

    @Override
    public boolean hasEnoughStock(Integer sizeId, int quantity) {
        ProductSize size = getById(sizeId);
        return size.getQuantity() >= quantity;
    }

    @Override
    public void changeStock(Integer sizeId, int delta) {
        ProductSize size = getById(sizeId);

        int newQty = size.getQuantity() + delta;
        if (newQty < 0) {
            throw new RuntimeException("Not enough stock");
        }

        size.setQuantity(newQty);
        productSizeRepository.save(size);
    }

    // ================= BULK UPSERT =================

    @Override
    public void bulkUpsert(Integer productId, List<ProductSizeRequest> requests) {

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductSize> existingSizes =
            productSizeRepository.findByProduct_ProductId(productId);

        Map<Integer, ProductSize> sizeMap = existingSizes.stream()
            .collect(Collectors.toMap(
                ProductSize::getProductSizeId,
                Function.identity()
            ));

        // Update hoặc Create
        for (ProductSizeRequest req : requests) {
            if (req.getProductSizeId() != null && sizeMap.containsKey(req.getProductSizeId())) {
                // UPDATE
                ProductSize size = sizeMap.get(req.getProductSizeId());
                size.setSizeName(req.getSizeName());
                size.setPrice(req.getPrice());
                size.setDiscount(req.getDiscount());
                size.setQuantity(req.getQuantity());
            } else {
                // CREATE
                ProductSize size = new ProductSize();
                size.setProduct(product);
                size.setSizeName(req.getSizeName());
                size.setPrice(req.getPrice());
                size.setDiscount(
                    req.getDiscount() == null ? 0 : req.getDiscount()
                );
                size.setQuantity(
                    req.getQuantity() == null ? 0 : req.getQuantity()
                );
                productSizeRepository.save(size);
            }
        }

        // DELETE các size không còn gửi lên
        List<Integer> requestIds = requests.stream()
            .map(ProductSizeRequest::getProductSizeId)
            .filter(id -> id != null)
            .toList();

        existingSizes.stream()
            .filter(size -> !requestIds.contains(size.getProductSizeId()))
            .forEach(productSizeRepository::delete);
    }
}
