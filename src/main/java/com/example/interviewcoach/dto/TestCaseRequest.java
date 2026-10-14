package com.example.interviewcoach.dto;

public record TestCaseRequest(
        Long id,
        String input,
        String expectedOutput,
        String testCaseType
) {
}
