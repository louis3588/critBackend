package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.Review;
import com.lp.criticabackend.repos.ReviewRepository;
import com.lp.criticabackend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Review>> getAllReviewsByUsername(@PathVariable String username) {
        List<Review> reviews = reviewService.getReviewsByUsername(username);
        if(reviews.isEmpty()){
            return ResponseEntity.status(401).body(reviews);
        } else {
            return ResponseEntity.ok(reviews);
        }
    }

    @PostMapping("/{username}")
    public ResponseEntity<Review> createReview(@RequestBody Review review, @PathVariable String username) {
        Review saved = reviewService.save(review, username);
        if (saved == null) {
            return ResponseEntity.status(401).body(review);
        } else {
            return ResponseEntity.ok(saved);
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> editReview(@RequestBody Review review, @PathVariable Integer reviewId) {
        Review updated = reviewService.editReview(reviewId, review);
        return ResponseEntity.ok(updated);
    }
}
