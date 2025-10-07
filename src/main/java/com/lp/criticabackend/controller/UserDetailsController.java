package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.User;
import com.lp.criticabackend.model.UserDetails;
import com.lp.criticabackend.service.UserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/userDetails")
public class UserDetailsController {

    private final UserDetailsService userDetailsService;

    public UserDetailsController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> updateDetails(@PathVariable String username, @RequestBody UserDetails details){
        return ResponseEntity.ok(userDetailsService.updateUserDetails(username, details));
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getDetails(@PathVariable String username){
        Optional<UserDetails> details = userDetailsService.getDetails(username);
        return ResponseEntity.ok(details.orElse(new UserDetails()));
    }
}
