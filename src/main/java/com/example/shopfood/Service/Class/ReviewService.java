package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.OrderStatus;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.Review;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.Review.CreateReview;
import com.example.shopfood.Model.Request.Review.UpdateReview;
import com.example.shopfood.Repository.OrderRepository;
import com.example.shopfood.Repository.ProductRepository;
import com.example.shopfood.Repository.ReviewRepository;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Service.IReviewService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService implements IReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<Review> getAllReview() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findByReviewId(int reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public Review updateReview(int reviewId, UpdateReview request) throws IOException {
        Review existingReview = reviewRepository.findById(reviewId).orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByFullName(fullName).orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getRating() != null) {
            existingReview.setRating(request.getRating());
        }

        if (request.getReviewText() != null) {
            existingReview.setReviewText(request.getReviewText());
        }

        return reviewRepository.save(existingReview);
    }

    public void createReview(CreateReview request) throws IOException {

        // 1️⃣ Lấy user hiện tại
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Lấy product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + request.getProductId()));

        // 3️⃣ Kiểm tra user đã mua sản phẩm chưa
        boolean hasPurchased = orderRepository
                .existsByUserAndOrderDetailsProductAndOrderStatus(user, product, OrderStatus.COMPLETED);
        if (!hasPurchased) {
            throw new RuntimeException("Bạn chỉ được đánh giá sản phẩm đã mua");
        }

        // 4️⃣ Kiểm tra user đã review sản phẩm này chưa
        boolean alreadyReviewed = reviewRepository.existsByUserAndProduct(user, product);
        if (alreadyReviewed) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // 5️⃣ Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating phải từ 1 đến 5");
        }

        // 6️⃣ Tạo review và lưu
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }


    public void deleteByReviewId(int reviewId) {
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByFullName(fullName).orElseThrow(() -> new RuntimeException("User not found"));
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("You are not authorized to delete this review");
        } else {
            reviewRepository.delete(review);
        }
    }
}

