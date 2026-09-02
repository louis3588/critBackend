package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.Review;
import com.lp.criticabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findReviewsByUser(User user);
}
