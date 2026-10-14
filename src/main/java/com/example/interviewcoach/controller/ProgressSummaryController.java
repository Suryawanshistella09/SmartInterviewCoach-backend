package com.example.interviewcoach.controller;

import com.example.interviewcoach.dto.ProgressSummaryResponse;
import com.example.interviewcoach.service.ProgressSummaryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/progress")
public class ProgressSummaryController {

    private final ProgressSummaryService progressSummaryService;

    public ProgressSummaryController(ProgressSummaryService progressSummaryService) {
        this.progressSummaryService = progressSummaryService;
    }

    @GetMapping("/summary/me")
    public ProgressSummaryResponse getMyProgressSummary(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated student is required"
            );
        }

        return progressSummaryService.getSummary(authentication.getName());
    }
}
