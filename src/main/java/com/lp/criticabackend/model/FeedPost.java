package com.lp.criticabackend.model;

import com.lp.criticabackend.model.review.Review;
import com.lp.criticabackend.model.review.ReviewComment;
import com.lp.criticabackend.model.review.ReviewLike;

import java.util.List;

public class FeedPost {

    private Review review;
    private User user;
    private Song song;
    private List<ReviewLike> likes;
    private List<ReviewComment> comments;
    private boolean userLiked;

    public FeedPost(Review review, User user, Song song, List<ReviewLike> likes, boolean userLiked, List<ReviewComment> comments) {
        this.review = review;
        this.user = user;
        this.song = song;
        this.likes = likes;
        this.userLiked = userLiked;
        this.comments = comments;
    }

    public Review getReview() {
        return review;
    }

    public User getUser() {
        return user;
    }

    public Song getSong() {
        return song;
    }

    public List<ReviewLike> getLikes() {
        return likes;
    }

    public boolean isUserLiked() {
        return userLiked;
    }
}
