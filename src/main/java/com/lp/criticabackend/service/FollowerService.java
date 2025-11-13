package com.lp.criticabackend.service;

import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Follow;
import com.lp.criticabackend.model.FollowStatus;
import com.lp.criticabackend.model.User;
import com.lp.criticabackend.repos.FollowRepository;
import com.lp.criticabackend.repos.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FollowerService {

    private final FollowRepository followRepo;
    private final UserRepository userRepo;

    private static final AppLogger log = AppLogger.getLogger(FollowerService.class);

    public FollowerService(FollowRepository followRepo, UserRepository userRepo) {
        this.followRepo = followRepo;
        this.userRepo = userRepo;
    }

    public int getUserIdByUsername(String username) {
        User notFound = new User();
        notFound.setIdusers(-1);
        User user = userRepo.findByUsername(username).orElse(notFound);
        return user.getIdusers();
    }

    public Follow sendFollowRequest(Integer followerId, Integer followedId) {
        if(followerId.equals(followedId)) {
            log.warn("Someone tried to follow themselves");
        }

        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User followed = userRepo.findById(followedId).orElseThrow(() -> new RuntimeException("Followed not found"));

        Optional<Follow> existing = followRepo.findByFollowerAndFollowing(follower, followed);
        if(existing.isPresent()) {
            log.error("Request already exists");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(followed);
        follow.setStatus(FollowStatus.PENDING);

        return followRepo.save(follow);
    }

    public Follow acceptOrRejectFollowRequest(Integer followerId, Integer followedId, FollowStatus newStatus) {
        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User followed = userRepo.findById(followedId).orElseThrow(() -> new RuntimeException("Followed not found"));
        Follow follow = followRepo.findByFollowerAndFollowing(follower, followed).orElseThrow(() -> new RuntimeException("Request not found"));
        if(follow.getStatus().equals(FollowStatus.PENDING)) {
            if(newStatus.equals(FollowStatus.ACCEPTED)) {
                follow.setStatus(FollowStatus.ACCEPTED);
            } else if (newStatus.equals(FollowStatus.REJECTED)) {
                follow.setStatus(FollowStatus.REJECTED);
            }
        }
        return followRepo.save(follow);
    }

    public List<User> getPendingRequests(Integer userid){
        User user = userRepo.findById(userid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return followRepo
                .findByFollowerAndStatus(user, FollowStatus.PENDING)
                .stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
    }

    public List<User> getFollowers(Integer userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return followRepo
                .findByFollowingAndStatus(user, FollowStatus.ACCEPTED)
                .stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
    }

    public List<User> getFollowing(Integer userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return followRepo
                .findByFollowerAndStatus(user, FollowStatus.ACCEPTED)
                .stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
    }

    public List<User> getMutuals(Integer userId){
        List<User> following = getFollowing(userId);
        List<User> followers = getFollowers(userId);

        return following
                .stream()
                .filter(followers::contains)
                .collect(Collectors.toList());
    }

    public ResponseEntity<Map<String, Object>> successFormatter(Follow follow){
        Map<String, Object> response = new HashMap<>();
        response.put("follower", follow.getFollower().getUsername());
        response.put("following", follow.getFollowing().getUsername());
        response.put("status", follow.getStatus().toString());
        return ResponseEntity.ok(response);
    }
}
