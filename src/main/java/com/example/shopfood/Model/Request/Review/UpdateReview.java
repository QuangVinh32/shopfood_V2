package com.example.shopfood.Model.Request.Review;

import lombok.Data;

@Data
public class UpdateReview {
    private Integer rating;
    private String reviewText;
}
