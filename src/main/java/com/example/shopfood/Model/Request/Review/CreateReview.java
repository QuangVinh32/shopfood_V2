package com.example.shopfood.Model.Request.Review;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CreateReview {
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private Integer productId;
}
