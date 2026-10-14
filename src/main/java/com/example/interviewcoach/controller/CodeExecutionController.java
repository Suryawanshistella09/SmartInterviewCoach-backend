package com.example.interviewcoach.controller;

import com.example.interviewcoach.dto.CodeExecutionRequest;
import com.example.interviewcoach.dto.CodeSubmissionResponse;
import com.example.interviewcoach.dto.CodingSubmissionHistoryResponse;
import com.example.interviewcoach.dto.RunCodeResponse;
import com.example.interviewcoach.service.CodeExecutionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coding")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/run")
    public RunCodeResponse run(@RequestBody CodeExecutionRequest request) {
        return codeExecutionService.run(request);
    }

    @GetMapping("/submissions/me")
    public List<CodingSubmissionHistoryResponse> getMySubmissions(
            Authentication authentication
    ) {
        return codeExecutionService.getStudentSubmissionHistory(
                authentication.getName()
        );
    }

    @PostMapping("/submit")
    public CodeSubmissionResponse submit(
            @RequestBody CodeExecutionRequest request,
            Authentication authentication
    ) {
        return codeExecutionService.submit(request, authentication.getName());
    }
}
