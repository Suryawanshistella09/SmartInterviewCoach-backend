package com.example.interviewcoach.controller;

import com.example.interviewcoach.dto.CodingProblemRequest;
import com.example.interviewcoach.dto.CodingProblemResponse;
import com.example.interviewcoach.service.CodingProblemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coding-problems")
@CrossOrigin(origins = "http://localhost:5173")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;

    public CodingProblemController(CodingProblemService codingProblemService) {
        this.codingProblemService = codingProblemService;
    }

    @GetMapping
    public List<CodingProblemResponse> getStudentProblems() {
        return codingProblemService.getStudentProblems();
    }

    @GetMapping("/{id}")
    public CodingProblemResponse getStudentProblem(@PathVariable Long id) {
        return codingProblemService.getStudentProblem(id);
    }

    @GetMapping("/admin")
    public List<CodingProblemResponse> getAdminProblems() {
        return codingProblemService.getAdminProblems();
    }

    @GetMapping("/admin/{id}")
    public CodingProblemResponse getAdminProblem(@PathVariable Long id) {
        return codingProblemService.getAdminProblem(id);
    }

    @PostMapping
    public CodingProblemResponse createProblem(@RequestBody CodingProblemRequest request) {
        return codingProblemService.createProblem(request);
    }

    @PutMapping("/{id}")
    public CodingProblemResponse updateProblem(
            @PathVariable Long id,
            @RequestBody CodingProblemRequest request
    ) {
        return codingProblemService.updateProblem(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteProblem(@PathVariable Long id) {
        codingProblemService.deleteProblem(id);
    }
}
