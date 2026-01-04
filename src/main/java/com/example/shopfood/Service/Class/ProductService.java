package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.DTO.ProductForAdmin;
import com.example.shopfood.Model.DTO.ProductForUser;
import com.example.shopfood.Model.DTO.ProductSizeDTO;
import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Model.Request.Product.CreateProduct;
import com.example.shopfood.Model.Request.Product.CreateProductSizeRequest;
import com.example.shopfood.Model.Request.Product.FilterProduct;
import com.example.shopfood.Model.Request.Product.UpdateProduct;
import com.example.shopfood.Repository.CategoryRepository;
import com.example.shopfood.Repository.ProductImageRepository;
import com.example.shopfood.Repository.ProductRepository;
import com.example.shopfood.Repository.ReviewRepository;
import com.example.shopfood.Service.IFileService;
import com.example.shopfood.Service.IProductService;
import com.example.shopfood.Specification.ProductSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IProductService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private IFileService fileService;
    @Autowired
    private ReviewRepository reviewRepository;
//    @Autowired
//    private ProductImageRepository productImageRepository;


    public Page<Product> getAllProductsPage(Pageable pageable, FilterProduct filterProduct) {
        Specification<Product> spec = ProductSpecification.buildSpec(filterProduct);
        return productRepository.findAll(spec, pageable);
    }

    public ProductForAdmin getProductByIdForAdmin(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        ProductForAdmin dto = new ProductForAdmin();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setCategoryStatus(product.getCategory().getCategoryStatus());

        // Ảnh
        List<String> imageUrls = product.getProductImages()
                .stream()
                .map(img -> "http://localhost:8080/files/image/" + img.getProductImageName())
                .toList();
        dto.setProductImages(imageUrls);

        // Size
        List<ProductSizeDTO> sizeDTOs = product.getSizes()
                .stream()
                .map(size -> {
                    ProductSizeDTO s = new ProductSizeDTO();
                    s.setSizeName(size.getSizeName().name());
                    s.setPrice(size.getPrice());
                    s.setDiscount(size.getDiscount());
                    s.setQuantity(size.getQuantity());
                    return s;
                })
                .toList();
        dto.setSizes(sizeDTOs);

        return dto;
    }


    @Transactional(rollbackFor = Exception.class)
    public void createProduct(CreateProduct createProduct) throws Exception {
        Category category = categoryRepository.findById(createProduct.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Product product = new Product();
        product.setProductName(createProduct.getProductName());
        product.setDescription(createProduct.getDescription());
        product.setCategory(category);

        // 1️⃣ xử lý ảnh
        List<ProductImage> images = new ArrayList<>();
        if (createProduct.getProductImages() != null) {
            for (MultipartFile file : createProduct.getProductImages()) {
                String path = fileService.uploadImage(file);
                ProductImage image = new ProductImage();
                image.setProductImageName(new File(path).getName());
                image.setProductImagePath(path);
                image.setProduct(product);
                images.add(image);
            }
        }
        product.setProductImages(images);

        // 2️⃣ lưu product trước để có productId
        productRepository.save(product);

        // 3️⃣ tạo ProductSize
        List<ProductSize> sizes = new ArrayList<>();
            if (createProduct.getProductSizes() != null) {
            for (ProductSize sReq : createProduct.getProductSizes()) {
                ProductSize size = new ProductSize();
                size.setProduct(product);
                size.setSizeName(sReq.getSizeName());
                size.setPrice(sReq.getPrice());
                size.setDiscount(sReq.getDiscount());
                size.setQuantity(sReq.getQuantity());
                sizes.add(size);
            }
        }
        product.setSizes(sizes);

        // 4️⃣ lưu product lần nữa để cascade lưu ProductSize
        productRepository.save(product);
    }



    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(int productId, UpdateProduct updateProduct) throws Exception {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        // Cập nhật tên
        if (updateProduct.getProductName() != null) {
            existingProduct.setProductName(updateProduct.getProductName());
        }

        // Cập nhật mô tả
        if (updateProduct.getDescription() != null) {
            existingProduct.setDescription(updateProduct.getDescription());
        }

        // Cập nhật category
        if (updateProduct.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateProduct.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + updateProduct.getCategoryId()));
            existingProduct.setCategory(category);
        }

        // Cập nhật ảnh
        if (updateProduct.getProductImages() != null) {
            List<ProductImage> images = new ArrayList<>();
            for (MultipartFile file : updateProduct.getProductImages()) {
                String path = fileService.uploadImage(file);
                ProductImage image = new ProductImage();
                image.setProduct(existingProduct);
                image.setProductImageName(new File(path).getName());
                image.setProductImagePath(path);
                images.add(image);
            }
            existingProduct.getProductImages().clear(); // xóa ảnh cũ
            existingProduct.getProductImages().addAll(images);
        }

        // Cập nhật size
        if (updateProduct.getSizes() != null) {
            // Xóa size cũ
            existingProduct.getSizes().clear();
            List<ProductSize> sizes = updateProduct.getSizes().stream().map(s -> {
                ProductSize size = new ProductSize();
                size.setProduct(existingProduct);
                size.setSizeName(ProductSizeEnum.valueOf(s.getSizeName()));
                size.setPrice(s.getPrice());
                size.setDiscount(s.getDiscount());
                size.setQuantity(s.getQuantity());
                return size;
            }).toList();

            existingProduct.getSizes().addAll(sizes);
        }

        productRepository.save(existingProduct);
    }


    public void deleteProduct(int id) {
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        productRepository.delete(existingProduct);
    }

    public boolean isProductNameExists(String productName) {
        return productRepository.existsByProductName(productName);
    }

    public ProductForUser getProductByIdForUser(int id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        List<Review> reviews = reviewRepository.findByProductProductId(id);
        return new ProductForUser(product, reviews);
    }
}
