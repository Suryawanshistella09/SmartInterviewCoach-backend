package com.example.interviewcoach.controller;

import com.example.interviewcoach.entity.InterviewResult;
import com.example.interviewcoach.service.InterviewResultService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/interview-results")
@CrossOrigin(origins = "http://localhost:5173")
public class InterviewResultController {

    private final InterviewResultService interviewResultService;

    public InterviewResultController(
            InterviewResultService interviewResultService) {

        this.interviewResultService = interviewResultService;
    }


    // =====================================================
    // SAVE INTERVIEW RESULT
    // =====================================================

    @PostMapping
    public ResponseEntity<InterviewResult> saveResult(
            @RequestBody InterviewResult result,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user is required"
            );
        }

        // The JWT principal is authoritative; never accept another user's email
        // from the request body as the owner of a saved result.
        result.setUserEmail(authentication.getName());

        InterviewResult savedResult =
                interviewResultService.saveResult(result);

        return ResponseEntity.ok(savedResult);
    }


    // =====================================================
    // GET RESULTS OF A USER
    // =====================================================

    @GetMapping("/user/{email}")
    public ResponseEntity<List<InterviewResult>> getUserResults(
            @PathVariable String email,
            Authentication authentication
    ) {

        if (authentication == null
                || authentication.getName() == null
                || !email.equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view your own interview results"
            );
        }

        List<InterviewResult> results =
                interviewResultService.getResultsByUser(email);

        return ResponseEntity.ok(results);
    }


    // =====================================================
    // GET ALL RESULTS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<InterviewResult>> getAllResults() {

        List<InterviewResult> results =
                interviewResultService.getAllResults();

        return ResponseEntity.ok(results);
    }
}
