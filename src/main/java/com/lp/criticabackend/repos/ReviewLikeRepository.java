package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.User;
import com.lp.criticabackend.model.review.Review;
import com.lp.criticabackend.model.review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Integer> {

    Optional<ReviewLike> findReviewLikeByReviewAndUser(Review review, User user);
    List<ReviewLike> findByReview(Review review);
}
