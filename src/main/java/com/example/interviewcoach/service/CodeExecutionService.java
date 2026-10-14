package com.example.interviewcoach.service;

import com.example.interviewcoach.dto.CodeExecutionRequest;
import com.example.interviewcoach.dto.CodeSubmissionResponse;
import com.example.interviewcoach.dto.CodingSubmissionHistoryResponse;
import com.example.interviewcoach.dto.RunCaseResult;
import com.example.interviewcoach.dto.RunCodeResponse;
import com.example.interviewcoach.entity.CodingProblem;
import com.example.interviewcoach.entity.CodingSubmission;
import com.example.interviewcoach.entity.TestCase;
import com.example.interviewcoach.repository.CodingProblemRepository;
import com.example.interviewcoach.repository.CodingSubmissionRepository;
import com.example.interviewcoach.repository.TestCaseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CodeExecutionService {

    private static final Logger logger =
            LoggerFactory.getLogger(CodeExecutionService.class);

    private static final int MAX_SOURCE_LENGTH = 100_000;
    private static final int MAX_INPUT_LENGTH = 100_000;
    private static final int MAX_TEST_CASES = 50;
    private static final int MAX_OUTPUT_BYTES = 10_000;
    private static final int MAX_ERROR_LENGTH = 10_000;

    private static final long EXECUTION_TIMEOUT_MILLIS = 25_000;
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Kolkata");

    private final CodingProblemRepository codingProblemRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodingSubmissionRepository codingSubmissionRepository;

    private final RestClient restClient;

    private final String apiUrl;
    private final String authToken;
    private final String rapidApiKey;
    private final String rapidApiHost;

    public CodeExecutionService(
            CodingProblemRepository codingProblemRepository,
            TestCaseRepository testCaseRepository,
            CodingSubmissionRepository codingSubmissionRepository,
            @Value("${JUDGE0_API_URL:${judge0.api.url:}}") String apiUrl,
            @Value("${JUDGE0_AUTH_TOKEN:}") String authToken,
            @Value("${JUDGE0_RAPIDAPI_KEY:}") String rapidApiKey,
            @Value("${JUDGE0_RAPIDAPI_HOST:}") String rapidApiHost
    ) {

        this.codingProblemRepository = codingProblemRepository;
        this.testCaseRepository = testCaseRepository;
        this.codingSubmissionRepository = codingSubmissionRepository;

        this.apiUrl = apiUrl == null
                ? ""
                : apiUrl.trim().replaceAll("/+$", "");

        this.authToken = authToken;
        this.rapidApiKey = rapidApiKey;
        this.rapidApiHost = rapidApiHost;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(25));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    // RUN CODE

    @Transactional(readOnly = true)
    public List<CodingSubmissionHistoryResponse> getStudentSubmissionHistory(
            String studentEmail
    ) {
        if (studentEmail == null || studentEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated student is required"
            );
        }

        List<CodingSubmission> submissions = codingSubmissionRepository
                .findByUserEmailOrderBySubmittedAtDescIdDesc(studentEmail);

        Set<Long> problemIds = new HashSet<>();
        for (CodingSubmission submission : submissions) {
            if (submission.getCodingProblemId() != null) {
                problemIds.add(submission.getCodingProblemId());
            }
        }

        Map<Long, CodingProblem> problemsById = new HashMap<>();
        for (CodingProblem problem : codingProblemRepository.findAllById(problemIds)) {
            problemsById.put(problem.getId(), problem);
        }

        return submissions.stream()
                .map(submission -> {
                    Long problemId = submission.getCodingProblemId();
                    CodingProblem problem = problemId == null
                            ? null
                            : problemsById.get(problemId);
                    String problemTitle = problem == null
                            ? problemId == null ? "Unknown problem" : "Problem #" + problemId
                            : problem.getTitle();
                    String problemDifficulty = problem == null
                            ? null
                            : problem.getDifficulty();

                    return new CodingSubmissionHistoryResponse(
                            submission.getId(),
                            problemId,
                            problemTitle,
                            problemDifficulty,
                            submission.getLanguage(),
                            submission.getScore(),
                            submission.getVerdict(),
                            submission.getPassedCases(),
                            submission.getTotalCases(),
                            submission.getSubmittedAt()
                    );
                })
                .toList();
    }

    public RunCodeResponse run(CodeExecutionRequest request) {

        ValidatedRequest validated = validateRequest(request);

        List<TestCase> sampleCases = testCaseRepository
                .findByCodingProblem_IdAndHiddenFalseOrderByIdAsc(
                        validated.problemId()
                );

        validateTestCases(sampleCases);

        List<RunCaseResult> results = new ArrayList<>();

        for (int index = 0; index < sampleCases.size(); index++) {

            TestCase testCase = sampleCases.get(index);

            ExecutionResult execution = execute(
                    validated.languageId(),
                    validated.code(),
                    testCase.getInput()
            );

            boolean passed =
                    execution.status().equals("Accepted")
                    && normalizeOutput(execution.stdout())
                    .equals(normalizeOutput(testCase.getExpectedOutput()));

            String status = passed
                    ? "Test Case Passed"
                    : execution.status().equals("Accepted")
                        ? "Test Case Failed"
                        : execution.status();

            results.add(new RunCaseResult(
                    index + 1,
                    status,
                    passed,
                    execution.stdout(),
                    testCase.getExpectedOutput(),
                    execution.errorMessage(),
                    execution.timeSeconds(),
                    execution.memoryKb()
            ));
        }

        int passedCases = (int) results.stream()
                .filter(RunCaseResult::passed)
                .count();

        return new RunCodeResponse(
                validated.problemId(),
                validated.language(),
                verdict(
                        results.stream()
                                .map(RunCaseResult::status)
                                .toList(),
                        passedCases,
                        results.size()
                ),
                passedCases,
                results.size(),
                results
        );
    }

    // SUBMIT CODE

    public CodeSubmissionResponse submit(
            CodeExecutionRequest request,
            String studentEmail
    ) {

        ValidatedRequest validated = validateRequest(request);

        if (studentEmail == null || studentEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated student is required"
            );
        }

        List<TestCase> testCases = testCaseRepository
                .findByCodingProblem_IdOrderByIdAsc(
                        validated.problemId()
                );

        validateTestCases(testCases);

        int passedCases = 0;

        List<String> statuses = new ArrayList<>();

        String compilerOutput = "";

        for (TestCase testCase : testCases) {

            ExecutionResult execution = execute(
                    validated.languageId(),
                    validated.code(),
                    testCase.getInput()
            );

            boolean passed =
                    execution.status().equals("Accepted")
                    && normalizeOutput(execution.stdout())
                    .equals(normalizeOutput(testCase.getExpectedOutput()));

            if (passed) {

                passedCases++;
                statuses.add("Accepted");

            } else {

                statuses.add(
                        execution.status().equals("Accepted")
                                ? "Wrong Answer"
                                : execution.status()
                );

                if (compilerOutput.isBlank()
                        && !execution.errorMessage().isBlank()) {

                    compilerOutput = truncate(
                            execution.errorMessage(),
                            MAX_ERROR_LENGTH
                    );
                }
            }
        }

        double score = 100.0 * passedCases / testCases.size();

        String submissionVerdict = verdict(
                statuses,
                passedCases,
                testCases.size()
        );

        LocalDateTime submittedAt = LocalDateTime.now(APPLICATION_ZONE);

        CodingSubmission submission = new CodingSubmission();

        submission.setUserEmail(studentEmail);
        submission.setCodingProblemId(validated.problemId());
        submission.setLanguage(validated.language());
        submission.setCode(validated.code());
        submission.setTotalCases(testCases.size());
        submission.setPassedCases(passedCases);
        submission.setScore(score);
        submission.setVerdict(submissionVerdict);
        submission.setCompilerOutput(compilerOutput);
        submission.setSubmittedAt(submittedAt);

        CodingSubmission saved =
                codingSubmissionRepository.save(submission);

        return new CodeSubmissionResponse(
                saved.getId(),
                validated.problemId(),
                validated.language(),
                submissionVerdict,
                passedCases,
                testCases.size(),
                score,
                submittedAt
        );
    }

    // VALIDATE REQUEST

    private ValidatedRequest validateRequest(
            CodeExecutionRequest request
    ) {

        if (request == null
                || request.problemId() == null
                || request.problemId() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid problemId is required"
            );
        }

        if (!codingProblemRepository.existsById(request.problemId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Coding problem not found"
            );
        }

        if (request.language() == null
                || request.language().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A supported language is required"
            );
        }

        String language = request.language().trim();

        int languageId = switch (
                language.toLowerCase(Locale.ROOT)
        ) {

            case "java" -> 62;
            case "python", "python 3" -> 71;
            case "c" -> 50;
            case "c++", "cpp" -> 54;

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supported languages are Java, Python, C, and C++"
            );
        };

        if (request.code() == null
                || request.code().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source code is required"
            );
        }

        if (request.code().length() > MAX_SOURCE_LENGTH
                || request.code()
                        .getBytes(StandardCharsets.UTF_8).length > 60_000) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source code exceeds the allowed size"
            );
        }

        requireConfigured();

        String canonicalLanguage = switch (languageId) {

            case 62 -> "Java";
            case 71 -> "Python";
            case 50 -> "C";
            default -> "C++";
        };

        return new ValidatedRequest(
                request.problemId(),
                canonicalLanguage,
                languageId,
                request.code()
        );
    }

    // VALIDATE TEST CASES

    private void validateTestCases(List<TestCase> testCases) {

        if (testCases == null || testCases.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "This problem has no test cases configured"
            );
        }

        if (testCases.size() > MAX_TEST_CASES) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Maximum 50 test cases are allowed"
            );
        }

        for (TestCase testCase : testCases) {

            if (testCase.getInput() == null
                    || testCase.getInput().length() > MAX_INPUT_LENGTH
                    || testCase.getExpectedOutput() == null
                    || testCase.getExpectedOutput()
                            .getBytes(StandardCharsets.UTF_8).length
                            > MAX_OUTPUT_BYTES) {

                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Invalid test case input or output"
                );
            }
        }
    }

    private void requireConfigured() {

        if (apiUrl.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Judge0 API URL is not configured"
            );
        }
    }

    // EXECUTE CODE USING JUDGE0

    private ExecutionResult execute(
            int languageId,
            String sourceCode,
            String stdin
    ) {

        try {

            logger.info(
                    "Sending code to Judge0. Language ID: {}",
                    languageId
            );

            Map<String, Object> response = restClient.post()

                    .uri(
                            apiUrl
                            + "/submissions?base64_encoded=false&wait=false"
                    )

                    .contentType(new MediaType(
                            MediaType.APPLICATION_JSON,
                            StandardCharsets.UTF_8
                    ))
                    .accept(MediaType.APPLICATION_JSON)

                    .headers(headers -> {

                        if (authToken != null && !authToken.isBlank()) {
                            headers.set("X-Auth-Token", authToken);
                        }

                        if (rapidApiKey != null && !rapidApiKey.isBlank()) {
                            headers.set("X-RapidAPI-Key", rapidApiKey);
                        }

                        if (rapidApiHost != null && !rapidApiHost.isBlank()) {
                            headers.set("X-RapidAPI-Host", rapidApiHost);
                        }
                    })

                    .body(toJudge0RequestJson(
                            sourceCode,
                            languageId,
                            stdin
                    ))

                    .retrieve()
                    .body(Map.class);

            String token = response == null
                    ? ""
                    : stringValue(response, "token");

            if (!token.isBlank()) {
                return pollForResult(token);
            }

            ExecutionResult result = parseResult(response);

            if (result.statusId() == 1
                    || result.statusId() == 2) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Judge0 did not return an execution token"
                );
            }

            return result;

        } catch (ResponseStatusException exception) {

            throw exception;

        } catch (RestClientResponseException exception) {

            logger.error(
                    "Judge0 HTTP error {}: {}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Judge0 rejected the execution request"
            );

        } catch (RestClientException exception) {

            logger.error(
                    "Judge0 execution failed: {}",
                    exception.getMessage()
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The code execution service is unavailable"
            );
        }
    }

    // GET EXECUTION RESULT

    private ExecutionResult pollForResult(String token) {

        long deadline =
                System.currentTimeMillis()
                + EXECUTION_TIMEOUT_MILLIS;

        String encodedToken =
                token.replaceAll("[^A-Za-z0-9_-]", "");

        if (encodedToken.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Judge0 returned an invalid token"
            );
        }

        while (System.currentTimeMillis() < deadline) {

            try {

                Thread.sleep(300);

            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();

                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Code execution interrupted"
                );
            }

            try {

                Map<String, Object> response = restClient.get()

                        .uri(
                                apiUrl
                                + "/submissions/"
                                + encodedToken
                                + "?base64_encoded=false"
                        )

                        .accept(MediaType.APPLICATION_JSON)

                        .headers(headers -> {

                            if (authToken != null && !authToken.isBlank()) {
                                headers.set("X-Auth-Token", authToken);
                            }

                            if (rapidApiKey != null
                                    && !rapidApiKey.isBlank()) {

                                headers.set(
                                        "X-RapidAPI-Key",
                                        rapidApiKey
                                );
                            }

                            if (rapidApiHost != null
                                    && !rapidApiHost.isBlank()) {

                                headers.set(
                                        "X-RapidAPI-Host",
                                        rapidApiHost
                                );
                            }
                        })

                        .retrieve()
                        .body(Map.class);

                ExecutionResult result = parseResult(response);

                if (result.statusId() != 1
                        && result.statusId() != 2) {

                    return result;
                }

            } catch (RestClientException exception) {

                logger.error(
                        "Judge0 polling failed: {}",
                        exception.getMessage()
                );

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Unable to fetch Judge0 execution result"
                );
            }
        }

        throw new ResponseStatusException(
                HttpStatus.GATEWAY_TIMEOUT,
                "Code execution timed out"
        );
    }

    // PARSE JUDGE0 RESPONSE

    private ExecutionResult parseResult(
            Map<String, Object> response
    ) {

        if (response == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Judge0 returned an empty response"
            );
        }

        Map<?, ?> status = mapValue(response.get("status"));

        int statusId = intValue(status.get("id"), -1);

        if (statusId < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Invalid Judge0 execution response"
            );
        }

        String description =
                stringValue(status, "description");

        String lowerDescription =
                description.toLowerCase(Locale.ROOT);

        String statusLabel =
                lowerDescription.contains("memory limit")
                ? "Memory Limit Exceeded"
                : lowerDescription.contains("time limit")
                ? "Time Limit Exceeded"
                : switch (statusId) {

                    case 3 -> "Accepted";
                    case 4 -> "Wrong Answer";
                    case 5 -> "Time Limit Exceeded";
                    case 6 -> "Compilation Error";
                    case 8 -> "Output Limit Exceeded";
                    case 7, 9, 10, 11, 12 -> "Runtime Error";
                    case 13, 14 -> "Execution Error";
                    case 1, 2 -> description;
                    default -> "Execution Error";
                };

        String stdout =
                stringValue(response, "stdout");

        boolean outputExceeded =
                isOutputOverLimit(stdout);

        if (outputExceeded) {
            statusLabel = "Output Limit Exceeded";
        }

        String errorMessage = firstNonBlank(
                stringValue(response, "compile_output"),
                stringValue(response, "stderr"),
                outputExceeded
                        ? "Program output exceeded the allowed limit."
                        : "",
                statusLabel.equals("Accepted")
                        || statusLabel.equals("Wrong Answer")
                        ? ""
                        : description
        );

        Double timeSeconds =
                doubleValue(response.get("time"));

        Long memoryKb =
                longValue(response.get("memory"));

        return new ExecutionResult(
                statusId,
                statusLabel,
                truncateUtf8(stdout, MAX_OUTPUT_BYTES),
                truncate(errorMessage, MAX_ERROR_LENGTH),
                timeSeconds,
                memoryKb
        );
    }

    // CALCULATE VERDICT

    private String verdict(
            List<String> statuses,
            int passedCases,
            int totalCases
    ) {

        for (String status : List.of(
                "Compilation Error",
                "Time Limit Exceeded",
                "Memory Limit Exceeded",
                "Output Limit Exceeded",
                "Runtime Error",
                "Execution Error"
        )) {

            if (statuses.contains(status)) {
                return status;
            }
        }

        if (passedCases == totalCases) {
            return "Accepted";
        }

        return passedCases == 0
                ? "Wrong Answer"
                : "Partially Accepted";
    }

    private String normalizeOutput(String output) {

        return (output == null ? "" : output)
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }

    private String toJudge0RequestJson(
            String sourceCode,
            int languageId,
            String stdin
    ) {
        return "{\"source_code\":\""
                + escapeJsonString(sourceCode)
                + "\",\"language_id\":"
                + languageId
                + ",\"stdin\":\""
                + escapeJsonString(stdin == null ? "" : stdin)
                + "\"}";
    }

    private String escapeJsonString(String value) {
        StringBuilder escaped = new StringBuilder(value.length());

        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);

            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20
                            || Character.isSurrogate(character)) {
                        escaped.append("\\u");
                        String hex = Integer.toHexString(character);
                        escaped.append("0".repeat(4 - hex.length()))
                                .append(hex);
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }

        return escaped.toString();
    }

    private String firstNonBlank(String... values) {

        for (String value : values) {

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String truncate(String value, int maxLength) {

        if (value == null) {
            return "";
        }

        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength);
    }

    private String truncateUtf8(String value, int maxBytes) {

        if (value == null) {
            return "";
        }

        StringBuilder truncated = new StringBuilder();
        int byteLength = 0;

        for (int offset = 0; offset < value.length();) {

            int codePoint = value.codePointAt(offset);
            int codePointBytes = utf8Length(codePoint);

            if (byteLength + codePointBytes > maxBytes) {
                break;
            }

            truncated.appendCodePoint(codePoint);
            byteLength += codePointBytes;
            offset += Character.charCount(codePoint);
        }

        return truncated.toString();
    }

    private boolean isOutputOverLimit(String value) {

        if (value == null) {
            return false;
        }

        int byteLength = 0;

        for (int offset = 0; offset < value.length();) {

            int codePoint = value.codePointAt(offset);
            byteLength += utf8Length(codePoint);

            if (byteLength > MAX_OUTPUT_BYTES) {
                return true;
            }

            offset += Character.charCount(codePoint);
        }

        return false;
    }

    private int utf8Length(int codePoint) {

        if (codePoint <= 0x7F) {
            return 1;
        }

        if (codePoint <= 0x7FF) {
            return 2;
        }

        return codePoint <= 0xFFFF ? 3 : 4;
    }

    private Map<?, ?> mapValue(Object value) {

        return value instanceof Map<?, ?> map
                ? map
                : Map.of();
    }

    private String stringValue(
            Map<?, ?> map,
            String key
    ) {

        Object value = map.get(key);

        return value instanceof String text
                ? text
                : "";
    }

    private int intValue(Object value, int fallback) {

        return value instanceof Number number
                ? number.intValue()
                : fallback;
    }

    private Double doubleValue(Object value) {

        return value instanceof Number number
                ? number.doubleValue()
                : null;
    }

    private Long longValue(Object value) {

        return value instanceof Number number
                ? number.longValue()
                : null;
    }

    private record ValidatedRequest(
            Long problemId,
            String language,
            int languageId,
            String code
    ) {}

    private record ExecutionResult(
            int statusId,
            String status,
            String stdout,
            String errorMessage,
            Double timeSeconds,
            Long memoryKb
    ) {}
}
