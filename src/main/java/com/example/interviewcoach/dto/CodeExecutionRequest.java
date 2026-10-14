package com.example.interviewcoach.dto;

public record CodeExecutionRequest(
        Long problemId,
        String language,
        String code
) {
}
