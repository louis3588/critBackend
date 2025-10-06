package com.lp.criticabackend.service;

import com.lp.criticabackend.model.User;
import com.lp.criticabackend.model.UserDetails;
import com.lp.criticabackend.repos.UserDetailsRepository;
import com.lp.criticabackend.repos.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Service
public class UserDetailsService {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;

    public UserDetailsService(UserRepository userRepository, UserDetailsRepository userDetailsRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    public User getUserByEmail(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }

    public UserDetails updateUserDetails(String username, UserDetails userDetails) {
        User user = getUserByEmail(username);

        UserDetails details = userDetailsRepository
                .findByUser_Username(username)
                .orElse(new UserDetails());

        details.setUser(user);

        if(userDetails.getProfilePicture() != null) {
            details.setProfilePicture(userDetails.getProfilePicture());
        }

        if(userDetails.getDateOfBirth() != null) {
            details.setDateOfBirth(userDetails.getDateOfBirth());
        }

        if(userDetails.getFavouriteGenre() != null) {
            details.setFavouriteGenre(userDetails.getFavouriteGenre());
        }

        if (userDetails.getBio() != null) {
            details.setBio(userDetails.getBio());
        }

        if(userDetails.getLocation() != null) {
            details.setLocation(userDetails.getLocation());
        }

        return userDetailsRepository.save(details);
    }

    public Optional<UserDetails> getDetails(String username) {
        return userDetailsRepository.findByUser_Username(username);
    }
}
