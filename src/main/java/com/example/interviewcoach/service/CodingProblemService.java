package com.example.interviewcoach.service;

import com.example.interviewcoach.dto.CodingProblemRequest;
import com.example.interviewcoach.dto.CodingProblemResponse;
import com.example.interviewcoach.dto.TestCaseRequest;
import com.example.interviewcoach.dto.TestCaseResponse;
import com.example.interviewcoach.entity.CodingProblem;
import com.example.interviewcoach.entity.TestCase;
import com.example.interviewcoach.repository.CodingProblemRepository;
import com.example.interviewcoach.repository.TestCaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodingProblemService {

    private final CodingProblemRepository codingProblemRepository;
    private final TestCaseRepository testCaseRepository;

    public CodingProblemService(
            CodingProblemRepository codingProblemRepository,
            TestCaseRepository testCaseRepository
    ) {
        this.codingProblemRepository = codingProblemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Transactional(readOnly = true)
    public List<CodingProblemResponse> getStudentProblems() {
        return codingProblemRepository.findAll().stream()
                .map(problem -> toResponse(
                        problem,
                        testCaseRepository.findByCodingProblem_IdAndHiddenFalse(problem.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CodingProblemResponse getStudentProblem(Long id) {
        CodingProblem problem = getProblem(id);
        return toResponse(
                problem,
                testCaseRepository.findByCodingProblem_IdAndHiddenFalse(problem.getId())
        );
    }

    @Transactional(readOnly = true)
    public List<CodingProblemResponse> getAdminProblems() {
        return codingProblemRepository.findAll().stream()
                .map(problem -> toResponse(
                        problem,
                        testCaseRepository.findByCodingProblem_Id(problem.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CodingProblemResponse getAdminProblem(Long id) {
        CodingProblem problem = getProblem(id);
        return toResponse(
                problem,
                testCaseRepository.findByCodingProblem_Id(problem.getId())
        );
    }

    @Transactional
    public CodingProblemResponse createProblem(CodingProblemRequest request) {
        validate(request);

        CodingProblem problem = new CodingProblem();
        applyFields(problem, request);
        codingProblemRepository.save(problem);
        synchronizeTestCases(problem, request.testCases());
        return toResponse(problem, testCaseRepository.findByCodingProblem_Id(problem.getId()));
    }

    @Transactional
    public CodingProblemResponse updateProblem(Long id, CodingProblemRequest request) {
        validate(request);

        CodingProblem problem = getProblem(id);
        applyFields(problem, request);
        codingProblemRepository.save(problem);
        synchronizeTestCases(problem, request.testCases());
        return toResponse(problem, testCaseRepository.findByCodingProblem_Id(problem.getId()));
    }

    @Transactional
    public void deleteProblem(Long id) {
        CodingProblem problem = getProblem(id);
        testCaseRepository.deleteAll(
                testCaseRepository.findByCodingProblem_Id(problem.getId())
        );
        codingProblemRepository.delete(problem);
    }

    private CodingProblem getProblem(Long id) {
        return codingProblemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coding problem not found"
                ));
    }

    private void applyFields(CodingProblem problem, CodingProblemRequest request) {
        problem.setSubject(request.subject().trim());
        problem.setDifficulty(request.difficulty().trim());
        problem.setTitle(request.title().trim());
        problem.setDescription(request.description().trim());
        problem.setTopic(request.topic().trim());
        problem.setInputFormat(request.inputFormat().trim());
        problem.setOutputFormat(request.outputFormat().trim());
        problem.setConstraints(request.constraints().trim());
    }

    private void synchronizeTestCases(
            CodingProblem problem,
            List<TestCaseRequest> requestedCases
    ) {
        List<TestCase> existingCases =
                testCaseRepository.findByCodingProblem_Id(problem.getId());
        Map<Long, TestCase> existingById = new HashMap<>();
        existingCases.forEach(testCase -> existingById.put(testCase.getId(), testCase));

        List<TestCase> casesToSave = new ArrayList<>();
        for (TestCaseRequest requested : requestedCases) {
            TestCase testCase;
            if (requested.id() == null) {
                testCase = new TestCase();
            } else {
                testCase = existingById.remove(requested.id());
                if (testCase == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "A test case does not belong to this coding problem"
                    );
                }
            }

            testCase.setCodingProblem(problem);
            testCase.setInput(requested.input());
            testCase.setExpectedOutput(requested.expectedOutput());
            testCase.setHidden("Hidden".equals(requested.testCaseType()));
            casesToSave.add(testCase);
        }

        testCaseRepository.deleteAll(existingById.values());
        testCaseRepository.saveAll(casesToSave);
    }

    private void validate(CodingProblemRequest request) {
        if (request == null
                || isBlank(request.subject())
                || isBlank(request.difficulty())
                || isBlank(request.title())
                || isBlank(request.description())
                || isBlank(request.topic())
                || isBlank(request.inputFormat())
                || isBlank(request.outputFormat())
                || isBlank(request.constraints())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All coding problem fields are required"
            );
        }

        if (!List.of(
                "Arrays",
                "Strings",
                "Linked List",
                "Stack & Queue",
                "Searching & Sorting",
                "Trees",
                "Graphs",
                "Dynamic Programming"
        ).contains(request.subject().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coding subject");
        }
        if (!List.of("Easy", "Medium", "Hard").contains(request.difficulty().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid difficulty");
        }

        List<TestCaseRequest> cases = request.testCases();
        if (cases == null || cases.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one sample and one hidden test case are required"
            );
        }

        boolean hasSample = false;
        boolean hasHidden = false;
        for (TestCaseRequest testCase : cases) {
            if (testCase == null
                    || isBlank(testCase.input())
                    || isBlank(testCase.expectedOutput())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Test case input and expected output are required"
                );
            }
            if ("Sample".equals(testCase.testCaseType())) {
                hasSample = true;
            } else if ("Hidden".equals(testCase.testCaseType())) {
                hasHidden = true;
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Test case type must be Sample or Hidden"
                );
            }
        }
        if (!hasSample || !hasHidden) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one sample and one hidden test case are required"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private CodingProblemResponse toResponse(
            CodingProblem problem,
            List<TestCase> testCases
    ) {
        List<TestCaseResponse> testCaseResponses = testCases.stream()
                .map(testCase -> new TestCaseResponse(
                        testCase.getId(),
                        testCase.getInput(),
                        testCase.getExpectedOutput(),
                        testCase.isHidden() ? "Hidden" : "Sample"
                ))
                .toList();

        return new CodingProblemResponse(
                problem.getId(),
                problem.getSubject(),
                problem.getDifficulty(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getTopic(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getConstraints(),
                testCaseResponses
        );
    }
}
