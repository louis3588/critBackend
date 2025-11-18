package com.lp.criticabackend.controller;

import com.lp.criticabackend.model.User;
import com.lp.criticabackend.repos.UserRepository;
import com.lp.criticabackend.security.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final SessionUtil sessionUtil;
    public UserController(UserRepository userRepo, PasswordEncoder passwordEncoder, SessionUtil util) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.sessionUtil = util;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);

        return ResponseEntity.ok(savedUser);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<User> userOpt;
        if(email.contains("@")){
            userOpt = userRepo.findByEmail(email);
        } else {
            userOpt = userRepo.findByUsername(email);
        }
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password.");
        }

        String token = sessionUtil.generateToken(user.getEmail());

        Map<String, Object> savedUser = new HashMap<>();
        savedUser.put("id", user.getIdusers());
        savedUser.put("firstName", user.getFirstName());
        savedUser.put("lastName", user.getLastName());
        savedUser.put("username", user.getUsername());
        savedUser.put("email", user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("sessionToken", token);
        response.put("user", savedUser);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }
}
