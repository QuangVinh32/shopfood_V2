package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.Review;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class ProductForUser {
    private String productName;
//    private String productImage;
    private Double price;
    private Integer discount;
    private Integer quantity;
    private String description;
    private List<ReviewDTO> reviews = new ArrayList<>();

    public ProductForUser(Product product, List<Review> reviews) {
        this.productName = product.getProductName();
//        this.productImage = product.getProductImage();
        this.price = product.getPrice();
        this.discount = product.getDiscount();
        this.quantity = product.getQuantity();
        this.description = product.getDescription();
        if (reviews != null) {
            for(Review review : reviews) {
                this.reviews.add(new ReviewDTO(review));
            }
        }

    }
}
