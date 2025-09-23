package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.Review;
import com.lp.criticabackend.repos.ReviewRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    private final ReviewRepository reviewRepo;

    public ReviewController(ReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepo.findAll();
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewRepo.save(review);
    }
}
