package com.example.shopfood.Controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.example.shopfood.Model.DTO.ReviewDTO;
import com.example.shopfood.Model.Entity.Review;
import com.example.shopfood.Model.Request.Review.CreateReview;
import com.example.shopfood.Model.Request.Review.UpdateReview;
import com.example.shopfood.Service.IReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/reviews"})
public class ReviewController {
    @Autowired
    private IReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> dtos = reviewService.getAllReview()
                .stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable int id) {
        return reviewService.findByReviewId(id)
                .map(review -> ResponseEntity.ok(new ReviewDTO(review)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createReview(@RequestBody @Valid CreateReview request) {
        try {
            reviewService.createReview(request);
            return ResponseEntity.ok("Review created successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error while creating review");
        }
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<String> updateReview(@PathVariable int id,
                                               @RequestBody @Valid UpdateReview request) {
        try {
            reviewService.updateReview(id, request);
            return ResponseEntity.ok("Review updated successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Review not found or unauthorized");
        }
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<String> deleteReview(@PathVariable int id) {
        try {
            reviewService.deleteByReviewId(id);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("You are not authorized to delete this review");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Review not found");
        }
    }
}
