package com.example.interviewcoach.dto;

public record RunCaseResult(
        int testCaseNumber,
        String status,
        boolean passed,
        String actualOutput,
        String expectedOutput,
        String errorMessage,
        Double timeSeconds,
        Long memoryKb
) {
}
