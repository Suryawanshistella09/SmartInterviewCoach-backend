package com.example.interviewcoach.dto;

import java.util.List;

public record RunCodeResponse(
        Long problemId,
        String language,
        String verdict,
        int passedCases,
        int totalCases,
        List<RunCaseResult> testCases
) {
}
