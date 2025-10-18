package com.lp.criticabackend.service;

import com.lp.criticabackend.model.Review;
import com.lp.criticabackend.model.User;
import com.lp.criticabackend.repos.ReviewRepository;
import com.lp.criticabackend.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public Review save(Review review, String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        review.setUser(user);

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        review.setCreatedAt(currentTime);

        return reviewRepository.save(review);
    }

    public void deleteReview(Review review){
        reviewRepository.delete(review);
    }

    public Review editReview(Integer reviewId, Review updatedReview){
        Review existingReview = reviewRepository.findById(reviewId).orElse(new Review());
        boolean newReview = (existingReview.getCreatedAt() == null || existingReview.getCreatedAt().isEmpty());

        if(updatedReview.getTitle() != null || newReview){
            existingReview.setTitle(updatedReview.getTitle());
        }

        if(updatedReview.getPayload() != null || newReview){
            existingReview.setPayload(updatedReview.getPayload());
        }

        if(updatedReview.getRating() != null || newReview){
            existingReview.setRating(updatedReview.getRating());
        }

        if(updatedReview.getFirstListen() != null || newReview){
            existingReview.setFirstListen(updatedReview.getFirstListen());
        }

        if(newReview){
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            existingReview.setCreatedAt(currentTime);
        }

        return reviewRepository.save(existingReview);
    }

    public List<Review> getReviewsByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(user -> reviewRepository.findAll().stream()
                .filter(r -> r.getUser() != null && r.getUser().getIdusers().equals(user.getIdusers()))
                .toList()).orElseGet(ArrayList::new);

    }
}
