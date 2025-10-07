package com.lp.criticabackend.repos;

import com.lp.criticabackend.model.User;
import com.lp.criticabackend.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    Optional<UserDetails> findByUser(User user);
}
