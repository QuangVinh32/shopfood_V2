package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductForUser {
    private String productName;
    private String description;
    private List<String> productImages = new ArrayList<>();
    private List<ProductSizeDTO> sizes = new ArrayList<>();
    private List<ReviewDTO> reviews = new ArrayList<>();

    private Integer categoryId;
    private String categoryImage; // URL ảnh category
    private CategoryStatus categoryStatus;

    public ProductForUser(Product product, List<Review> reviews) {
        this.productName = product.getProductName();
        this.description = product.getDescription();

        // Lấy danh sách ảnh sản phẩm
        if (product.getProductImages() != null) {
            for (ProductImage img : product.getProductImages()) {
                this.productImages.add(convertToImageUrl(img.getProductImageName()));
            }
        }

        // Lấy danh sách size
        if (product.getSizes() != null) {
            for (ProductSize size : product.getSizes()) {
                this.sizes.add(new ProductSizeDTO(
                        size.getSizeName().name(),
                        size.getPrice(),
                        size.getDiscount(),
                        size.getQuantity()
                ));
            }
        }

        // Lấy review
        if (reviews != null) {
            for (Review review : reviews) {
                this.reviews.add(new ReviewDTO(review));
            }
        }

        // Lấy thông tin category
        if (product.getCategory() != null) {
            Category category = product.getCategory();
            this.categoryId = category.getCategoryId();
            this.categoryStatus = category.getCategoryStatus();

            // QUAN TRỌNG: Convert category image thành URL đầy đủ
            if (category.getCategoryImage() != null && !category.getCategoryImage().isEmpty()) {
                this.categoryImage = convertToImageUrl(category.getCategoryImage());
            } else {
                this.categoryImage = null; // Hoặc URL ảnh mặc định
            }
        }
    }

    // Phương thức chuyển đổi tên file thành URL đầy đủ
    private String convertToImageUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        // Kiểm tra nếu đã là URL đầy đủ
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return fileName;
        }

        // Nếu chỉ là tên file, thêm base URL
        return "http://localhost:8080/files/image/" + fileName;
    }
}