package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.Review;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ReviewDTO {
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private UserDTO userDTO;

    public ReviewDTO(Review review) {
        this.rating = review.getRating();
        this.reviewText = review.getReviewText();
        this.createdAt = review.getCreatedAt();
        if (review.getUser() != null) {
            this.userDTO = new UserDTO(review.getUser());
        }

    }
}