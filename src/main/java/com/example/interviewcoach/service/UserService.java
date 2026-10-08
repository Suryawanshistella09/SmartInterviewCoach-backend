package com.example.interviewcoach.service;
import com.example.interviewcoach.security.JwtService;
import com.example.interviewcoach.entity.User;
import com.example.interviewcoach.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // REGISTER
    public String registerUser(User user) {

        // Check whether email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already registered";
        }

        // Set default role
        user.setRole("STUDENT");

        // Save user to database
        userRepository.save(user);

        return "Registration successful";
    }

    // LOGIN
    public String loginUser(User user) {

        // Find user by email
        Optional<User> existingUser =
                userRepository.findByEmail(user.getEmail());

        // If email doesn't exist
        if (existingUser.isEmpty()) {
            return "User not found";
        }

        // Get user from database
        User dbUser = existingUser.get();

        // Check password
        if (!dbUser.getPassword().equals(user.getPassword())) {
            return "Invalid password";
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                dbUser.getEmail(),
                dbUser.getRole()
        );

        return token;
    }
}
