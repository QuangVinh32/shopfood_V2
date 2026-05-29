package com.example.shopfood.Model.Request.Review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReview {

    @Min(value = 1, message = "Rating tối thiểu 1")
    @Max(value = 5, message = "Rating tối đa 5")
    private Integer rating;

    @Size(max = 2000, message = "Review tối đa 2000 ký tự")
    private String reviewText;
}
