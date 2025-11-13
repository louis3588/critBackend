package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.Follow;
import com.lp.criticabackend.model.FollowStatus;
import com.lp.criticabackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User followed);

    List<Follow> findByFollowingAndStatus(User followed, FollowStatus status);

    List<Follow> findByFollowerAndStatus(User follower, FollowStatus status);

}