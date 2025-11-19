package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.Follow;
import com.lp.criticabackend.model.FollowStatus;
import com.lp.criticabackend.model.User;
import com.lp.criticabackend.service.FollowerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {
    private final FollowerService followService;

    public FollowController(FollowerService followService) {
        this.followService = followService;
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<?> searchByUser(@PathVariable String username) {
        List<User> users = followService.searchByUsername(username);
        if (users.isEmpty()) {
            return ResponseEntity.status(401).build();
        } else {
            return ResponseEntity.ok(users);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendFollow(@RequestParam String followerUsername, @RequestParam String followedUsername) {
        int followerId = followService.getUserIdByUsername(followerUsername);
        int followedId = followService.getUserIdByUsername(followedUsername);
        if(followedId < 0 || followerId < 0){
            return ResponseEntity.status(401).build();
        } else {
             return followService.successFormatter(followService.sendFollowRequest(followerId, followedId));
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFollow(@RequestParam String followerUsername, @RequestParam String followedUsername) {
        int followerId = followService.getUserIdByUsername(followerUsername);
        int followedId = followService.getUserIdByUsername(followedUsername);
        if(followedId < 0 || followerId < 0){
            return ResponseEntity.status(401).build();
        } else {
            return followService.successFormatter(followService.acceptOrRejectFollowRequest(followerId, followedId, FollowStatus.ACCEPTED));
        }
    }

    @PostMapping("/decline")
    public ResponseEntity<?> declineFollow(@RequestParam String followerUsername, @RequestParam String followedUsername) {
        int followerId = followService.getUserIdByUsername(followerUsername);
        int followedId = followService.getUserIdByUsername(followedUsername);
        if(followedId < 0 || followerId < 0){
            return ResponseEntity.status(401).build();
        } else {
            return followService.successFormatter(followService.acceptOrRejectFollowRequest(followerId, followedId, FollowStatus.REJECTED));
        }
    }

    @GetMapping("/pending/{username}")
    public ResponseEntity<List<User>> getPending(@PathVariable String username) {
        int userId = followService.getUserIdByUsername(username);
        if(userId < 0){
            return ResponseEntity.status(401).build();
        } else {
            return ResponseEntity.ok(followService.getPendingRequests(userId));
        }
    }

    @GetMapping("/followers/{username}")
    public ResponseEntity<List<User>> getFollowers(@PathVariable String username) {
        int userId = followService.getUserIdByUsername(username);
        if(userId < 0){
            return ResponseEntity.status(401).build();
        } else {
            return ResponseEntity.ok(followService.getFollowers(userId));
        }

    }

    @GetMapping("/following/{username}")
    public ResponseEntity<List<User>> getFollowing(@PathVariable String username) {
        int userId = followService.getUserIdByUsername(username);
        if(userId < 0){
            return ResponseEntity.status(401).build();
        } else {
            return ResponseEntity.ok(followService.getFollowing(userId));
        }

    }

    @GetMapping("/mutual/{username}")
    public ResponseEntity<List<User>> getMutuals(@PathVariable String username) {
        int userId = followService.getUserIdByUsername(username);
        if(userId < 0){
            return ResponseEntity.status(401).build();
        } else {
            return ResponseEntity.ok(followService.getMutuals(userId));
        }

    }
}
