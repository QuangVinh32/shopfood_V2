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
import com.example.shopfood.Service.IProductSizeService;
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

    @Autowired
    private IProductSizeService productSizeService;
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
    public void createProduct(CreateProduct req) throws Exception {

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Product product = new Product();
        product.setProductName(req.getProductName());
        product.setDescription(req.getDescription());
        product.setCategory(category);

        // 1️⃣ Images
        List<ProductImage> images = new ArrayList<>();
        if (req.getProductImages() != null) {
            for (MultipartFile file : req.getProductImages()) {
                String path = fileService.uploadImage(file);
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setProductImageName(new File(path).getName());
                image.setProductImagePath(path);
                images.add(image);
            }
        }
        product.setProductImages(images);

        // 2️⃣ Save product
        productRepository.save(product);

        // 3️⃣ GỌI ProductSizeService
        if (req.getProductSizes() != null) {
            productSizeService.bulkUpsert(product.getProductId(), req.getProductSizes());
        }
    }




    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(int productId, UpdateProduct req) throws Exception {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (req.getProductName() != null) {
            product.setProductName(req.getProductName());
        }

        if (req.getDescription() != null) {
            product.setDescription(req.getDescription());
        }

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            product.setCategory(category);
        }

        // Images
        if (req.getProductImages() != null) {
            product.getProductImages().clear();
            for (MultipartFile file : req.getProductImages()) {
                String path = fileService.uploadImage(file);
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setProductImageName(new File(path).getName());
                image.setProductImagePath(path);
                product.getProductImages().add(image);
            }
        }

        productRepository.save(product);

        // ✅ Update size đúng chỗ
        if (req.getSizes() != null) {
            productSizeService.bulkUpsert(productId, req.getSizes());
        }
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
