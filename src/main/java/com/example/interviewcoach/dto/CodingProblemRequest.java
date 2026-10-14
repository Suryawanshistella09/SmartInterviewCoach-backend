package com.example.interviewcoach.dto;

import java.util.List;

public record CodingProblemRequest(
        String subject,
        String difficulty,
        String title,
        String description,
        String topic,
        String inputFormat,
        String outputFormat,
        String constraints,
        List<TestCaseRequest> testCases
) {
}
