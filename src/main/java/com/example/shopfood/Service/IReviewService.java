package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.Review;
import com.example.shopfood.Model.Request.Review.CreateReview;
import com.example.shopfood.Model.Request.Review.UpdateReview;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
@Service
public interface IReviewService {

    List<Review> getAllReview();

    Optional<Review> findByReviewId(int reviewId);

    Review updateReview(int reviewId, UpdateReview request) throws IOException;

    void createReview(CreateReview request) throws IOException;

    void deleteByReviewId(int categoryId);
}
