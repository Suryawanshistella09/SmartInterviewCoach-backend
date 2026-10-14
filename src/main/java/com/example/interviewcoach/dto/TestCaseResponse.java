package com.example.interviewcoach.dto;

public record TestCaseResponse(
        Long id,
        String input,
        String expectedOutput,
        String testCaseType
) {
}
