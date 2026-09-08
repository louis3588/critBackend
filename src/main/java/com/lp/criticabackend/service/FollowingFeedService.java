package com.lp.criticabackend.service;

import com.lp.criticabackend.model.FeedPost;
import com.lp.criticabackend.model.Song;
import com.lp.criticabackend.model.User;
import com.lp.criticabackend.model.review.Review;
import com.lp.criticabackend.model.review.ReviewComment;
import com.lp.criticabackend.model.review.ReviewLike;
import com.lp.criticabackend.repos.ReviewCommentRepository;
import com.lp.criticabackend.repos.ReviewLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowingFeedService {

    @Autowired
    private final FollowerService followerService;

    @Autowired
    private final ReviewService reviewService;

    @Autowired
    private final SongSearchService songSearchService;

    @Autowired
    private final ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private final ReviewCommentRepository reviewCommentRepository;

    public FollowingFeedService(FollowerService followerService, ReviewService reviewService, SongSearchService songSearchService, ReviewLikeRepository reviewLikeRepository, ReviewCommentRepository reviewCommentRepository) {
        this.followerService = followerService;
        this.reviewService = reviewService;
        this.songSearchService = songSearchService;
        this.reviewLikeRepository = reviewLikeRepository;
        this.reviewCommentRepository = reviewCommentRepository;
    }

    private User getFromUsername(String username) {
        List<User> potentialUsers = followerService.searchByUsername(username);
        if (potentialUsers.isEmpty()) {
            return null;
        } else {
            return potentialUsers.get(0);
        }
    }

    private Review getFromSongId(String username, String songId){
        List<Review> reviews = reviewService.getReviewBySong(username, songId);
        if(reviews.isEmpty()) {
            return null;
        } else {
            return reviews.get(0);
        }
    }

    public List<FeedPost> getFollowingFeed(String username) {

        User user = getFromUsername(username);
        if (user == null) {
            return new ArrayList<>();
        }
        List<User> following = followerService.getFollowing(user.getIdusers());

        if(following.isEmpty()) {
            return new ArrayList<>();
        }

        List<Review> reviews = new ArrayList<>();
        for (User followedUser : following) {
            List<Review> followedReviews = reviewService.getReviewsByUsername(followedUser.getUsername());
            reviews.addAll(followedReviews);
        }

        return reviews
                .stream()
                .sorted((a, b) -> compareReviewDates(b.getCreatedAt(), a.getCreatedAt()))
                .map(review -> buildFeedPost(review, user))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private FeedPost buildFeedPost(Review review, User currentUser) {
        Song song = songSearchService.getBySongId(review.getSongId());
        if(song == null) {
            return null;
        }

        User reviewer = followerService.sanitisedUser(review.getUser());
        List<ReviewLike> likes = reviewLikeRepository.findByReview(review);

        boolean userLiked;
        if(likes.isEmpty()) {
            userLiked = false;
        } else {
            userLiked = likes.stream().anyMatch(l -> l.getUser().equals(currentUser));
        }

        List<ReviewComment> comments = reviewCommentRepository.findByReviewOrderByCreatedAtDesc(review);

        return new FeedPost(review, reviewer, song, likes, userLiked, comments);
    }

    public boolean likeReview(String songId, String username) {
        Review review = getFromSongId(username, songId);
        if (review == null) {
            return false;
        }

        User user = getFromUsername(username);
        if (user == null) {
            return false;
        }
        if(reviewLikeRepository.findReviewLikeByReviewAndUser(review, user).isEmpty()) {
            reviewLikeRepository.save(new ReviewLike(review, user, LocalDateTime.now()));
            return true;
        } else {
            return false;
        }
    }

    public boolean unlikeReview(String songId, String username) {

        Review review = getFromSongId(username, songId);
        if (review == null) {
            return false;
        }

        User user = getFromUsername(username);
        if (user == null) {
            return false;
        }

        Optional<ReviewLike> like = reviewLikeRepository.findReviewLikeByReviewAndUser(review, user);
        like.ifPresent(reviewLikeRepository::delete);
        return true;
    }

    public ReviewComment addComment(String songId, String username, String comment) {
        Review review = getFromSongId(username, songId);
        if (review == null) {
            return null;
        }

        User user = getFromUsername(username);
        if (user == null) {
            return null;
        }

        ReviewComment reviewComment = new ReviewComment(user, review, comment, LocalDateTime.now());
        return reviewCommentRepository.save(reviewComment);
    }

    private int compareReviewDates(String dateA, String dateB) {
        DateTimeFormatter reviewDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            LocalDateTime parsedA = LocalDateTime.parse(dateA, reviewDateFormat);
            LocalDateTime parsedB = LocalDateTime.parse(dateB, reviewDateFormat);
            return parsedA.compareTo(parsedB);
        } catch (Exception e) {
            return dateA.compareTo(dateB);
        }
    }

}
