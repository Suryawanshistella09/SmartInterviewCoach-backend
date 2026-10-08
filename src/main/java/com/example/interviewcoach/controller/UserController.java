package com.example.interviewcoach.controller;

import com.example.interviewcoach.entity.User;
import com.example.interviewcoach.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Register API
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // Login API
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.loginUser(user);
    }
}