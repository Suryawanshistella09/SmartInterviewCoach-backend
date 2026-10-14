package com.example.interviewcoach.dto;

import java.time.LocalDateTime;

public record CodeSubmissionResponse(
        Long submissionId,
        Long problemId,
        String language,
        String verdict,
        int passedCases,
        int totalCases,
        double score,
        LocalDateTime submittedAt
) {
}
