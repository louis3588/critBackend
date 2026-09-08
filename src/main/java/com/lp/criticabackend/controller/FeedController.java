package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.FeedPost;
import com.lp.criticabackend.model.review.ReviewComment;
import com.lp.criticabackend.service.FollowingFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    @Autowired
    private final FollowingFeedService followingFeedService;

    public FeedController(FollowingFeedService followingFeedService) {
        this.followingFeedService = followingFeedService;
    }

    @GetMapping("/following/{username}")
    public ResponseEntity<?> getFollowingFeed(@PathVariable String username){
        List<FeedPost> followingFeed = followingFeedService.getFollowingFeed(username);
        if(followingFeed.isEmpty()){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(followingFeed);
        }
    }

    @PostMapping("/like")
    public ResponseEntity<?> likeReview(@RequestParam String username, @RequestParam String songId){
        boolean success = followingFeedService.likeReview(songId, username);
        if(!success){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/unlike")
    public ResponseEntity<?> unlikeReview(@RequestParam String username, @RequestParam String songId){
        boolean success = followingFeedService.unlikeReview(songId, username);
        if(!success){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("comment")
    public ResponseEntity<?> comment(@RequestParam String username,@RequestParam String songId, @RequestParam String comment){
        ReviewComment reviewComment = followingFeedService.addComment(songId, username, comment);
        if(reviewComment == null){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.ok().body(reviewComment);
        }
    }

}
