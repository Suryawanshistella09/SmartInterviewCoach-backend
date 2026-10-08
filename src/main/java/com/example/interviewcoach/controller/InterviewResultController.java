package com.example.interviewcoach.controller;

import com.example.interviewcoach.entity.InterviewResult;
import com.example.interviewcoach.service.InterviewResultService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody InterviewResult result) {

        InterviewResult savedResult =
                interviewResultService.saveResult(result);

        return ResponseEntity.ok(savedResult);
    }


    // =====================================================
    // GET RESULTS OF A USER
    // =====================================================

    @GetMapping("/user/{email}")
    public ResponseEntity<List<InterviewResult>> getUserResults(
            @PathVariable String email) {

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