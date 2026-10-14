package com.example.interviewcoach.dto;

import java.util.List;

public record CodingProblemResponse(
        Long id,
        String subject,
        String difficulty,
        String title,
        String description,
        String topic,
        String inputFormat,
        String outputFormat,
        String constraints,
        List<TestCaseResponse> testCases
) {
}
