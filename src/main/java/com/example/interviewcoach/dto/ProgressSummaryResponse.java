package com.example.interviewcoach.dto;

public record ProgressSummaryResponse(
        long activitiesCompleted,
        long questionsAttempted,
        int averageScore,
        double bestScore,
        int currentStreak
) {
}
