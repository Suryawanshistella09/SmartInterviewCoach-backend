package com.example.interviewcoach.controller;

import com.example.interviewcoach.entity.User;
import com.example.interviewcoach.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==============================
    // GET PROFILE
    // ==============================

    @GetMapping("/{email}")
    public ResponseEntity<?> getProfile(
            @PathVariable String email) {

        return userRepository.findByEmail(email)
                .map(user -> {

                    Map<String, Object> profile = new HashMap<>();

                    profile.put("id", user.getId());
                    profile.put("name", user.getName());
                    profile.put("email", user.getEmail());
                    profile.put("role", user.getRole());

                    return ResponseEntity.ok(profile);
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }


    // ==============================
    // UPDATE PROFILE
    // ==============================

    @PutMapping("/{email}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String email,
            @RequestBody Map<String, String> request) {

        return userRepository.findByEmail(email)
                .map(user -> {

                    // ------------------------------
                    // Update Name
                    // ------------------------------

                    if (request.containsKey("name")) {

                        String newName = request.get("name");

                        if (newName == null || newName.trim().isEmpty()) {

                            return ResponseEntity
                                    .badRequest()
                                    .body("Name cannot be empty");
                        }

                        user.setName(newName.trim());
                    }


                    // ------------------------------
                    // Update Email
                    // ------------------------------

                    if (request.containsKey("email")) {

                        String newEmail = request.get("email");

                        if (newEmail == null || newEmail.trim().isEmpty()) {

                            return ResponseEntity
                                    .badRequest()
                                    .body("Email cannot be empty");
                        }

                        newEmail = newEmail.trim();


                        // Check whether another user
                        // already has this email

                        if (!newEmail.equalsIgnoreCase(user.getEmail())
                                && userRepository.existsByEmail(newEmail)) {

                            return ResponseEntity
                                    .badRequest()
                                    .body("Email already exists");
                        }

                        user.setEmail(newEmail);
                    }


                    // ------------------------------
                    // Save Updated User
                    // ------------------------------

                    User updatedUser = userRepository.save(user);


                    // ------------------------------
                    // Return Updated Profile
                    // ------------------------------

                    Map<String, Object> profile = new HashMap<>();

                    profile.put("id", updatedUser.getId());
                    profile.put("name", updatedUser.getName());
                    profile.put("email", updatedUser.getEmail());
                    profile.put("role", updatedUser.getRole());

                    return ResponseEntity.ok(profile);
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }
}