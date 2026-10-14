package com.example.interviewcoach.dto;

import java.time.LocalDateTime;

public record CodingSubmissionHistoryResponse(
        Long submissionId,
        Long problemId,
        String problemTitle,
        String problemDifficulty,
        String language,
        double score,
        String verdict,
        int passedCases,
        int totalCases,
        LocalDateTime submittedAt
) {
}
