package com.lp.criticabackend.service;

import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.model.Follow;
import com.lp.criticabackend.model.FollowStatus;
import com.lp.criticabackend.model.User;
import com.lp.criticabackend.model.UserDetails;
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
    private final UserDetailsService userDetails;

    private static final AppLogger log = AppLogger.getLogger(FollowerService.class);

    public FollowerService(FollowRepository followRepo, UserRepository userRepo, UserDetailsService userDetails) {
        this.followRepo = followRepo;
        this.userRepo = userRepo;
        this.userDetails = userDetails;
    }

    public int getUserIdByUsername(String username) {
        User notFound = new User();
        notFound.setIdusers(-1);
        User user = userRepo.findByUsername(username).orElse(notFound);
        return user.getIdusers();
    }

    public List<User> searchByUsername(String username) {
        return userRepo.findByUsernameContainingIgnoreCase(username);
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

    public ResponseEntity<Map<String, Object>> userSuccessFormatter(List<User> users){
        Map<String, Object> response = new HashMap<>();
        for(User user : users){
            String username = user.getUsername();
            Optional<UserDetails> details = userDetails.getDetails(username);
            response.put(user.getUsername(), user);
            if(details.isPresent()) {
                String detailsHeader = username + " details";
                response.put(detailsHeader, details.get());
            }
        }

        return ResponseEntity.ok(response);
    }
}
