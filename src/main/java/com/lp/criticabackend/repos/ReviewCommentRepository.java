package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.review.Review;
import com.lp.criticabackend.model.review.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Integer> {
    List<ReviewComment> findByReviewOrderByCreatedAtDesc(Review review);
}
