package com.example.shopfood.Controller;

import java.io.IOException;
import java.util.List;

import com.example.shopfood.Model.DTO.ReviewDTO;
import com.example.shopfood.Model.Entity.Review;
import com.example.shopfood.Model.Request.Review.CreateReview;
import com.example.shopfood.Model.Request.Review.UpdateReview;
import com.example.shopfood.Service.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/reviews"})
public class ReviewController {
    @Autowired
    private IReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReview());
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable int id) {
        return reviewService.findByReviewId(id).map((review) -> ResponseEntity.ok(new ReviewDTO(review))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createReview(@RequestBody CreateReview request) {
        try {
            reviewService.createReview(request);
            return ResponseEntity.ok("Review created successfully");
        } catch (IOException var3) {
            return ResponseEntity.status(500).body("Error while creating review");
        }
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<String> updateReview(@PathVariable int id, @RequestBody UpdateReview request) {
        try {
            reviewService.updateReview(id, request);
            return ResponseEntity.ok("Review updated successfully");
        } catch (Exception var4) {
            return ResponseEntity.status(404).body("Review not found or unauthorized");
        }
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<String> deleteReview(@PathVariable int id) {
        try {
            reviewService.deleteByReviewId(id);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (SecurityException var3) {
            return ResponseEntity.status(403).body("You are not authorized to delete this review");
        } catch (Exception var4) {
            return ResponseEntity.status(404).body("Review not found");
        }
    }
}

