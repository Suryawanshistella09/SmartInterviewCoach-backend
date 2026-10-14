package com.example.interviewcoach.service;

import com.example.interviewcoach.dto.ProgressSummaryResponse;
import com.example.interviewcoach.entity.CodingSubmission;
import com.example.interviewcoach.entity.InterviewResult;
import com.example.interviewcoach.repository.CodingSubmissionRepository;
import com.example.interviewcoach.repository.InterviewResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
public class ProgressSummaryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Kolkata");

    private final CodingSubmissionRepository codingSubmissionRepository;
    private final InterviewResultRepository interviewResultRepository;

    public ProgressSummaryService(
            CodingSubmissionRepository codingSubmissionRepository,
            InterviewResultRepository interviewResultRepository
    ) {
        this.codingSubmissionRepository = codingSubmissionRepository;
        this.interviewResultRepository = interviewResultRepository;
    }

    @Transactional(readOnly = true)
    public ProgressSummaryResponse getSummary(String authenticatedEmail) {
        List<CodingSubmission> submissions = codingSubmissionRepository
                .findByUserEmailOrderBySubmittedAtDescIdDesc(authenticatedEmail);
        List<InterviewResult> interviewResults = interviewResultRepository
                .findByUserEmail(authenticatedEmail);

        Map<Long, CodingSubmission> latestSubmissionByProblem = new HashMap<>();
        Set<LocalDate> activeDates = new HashSet<>();

        // The repository returns newest submissions first, so the first row per
        // problem is the one used for score calculations.
        for (CodingSubmission submission : submissions) {
            if (submission.getCodingProblemId() != null) {
                latestSubmissionByProblem.putIfAbsent(
                        submission.getCodingProblemId(),
                        submission
                );
            }

            addActiveDate(activeDates, submission.getSubmittedAt());
        }

        List<InterviewResult> qualifyingInterviews = interviewResults.stream()
                .filter(result -> isQualifyingInterview(result.getInterviewType()))
                .toList();

        for (InterviewResult interview : qualifyingInterviews) {
            addActiveDate(activeDates, interview.getCompletedAt());
        }

        long activitiesCompleted =
                (long) latestSubmissionByProblem.size() + qualifyingInterviews.size();
        long questionsAttempted = latestSubmissionByProblem.size()
                + qualifyingInterviews.stream()
                        .mapToLong(result -> Math.max(0, result.getTotalQuestions()))
                        .sum();

        List<Double> scores = new ArrayList<>();
        latestSubmissionByProblem.values().forEach(
                submission -> scores.add(submission.getScore())
        );
        qualifyingInterviews.forEach(
                interview -> scores.add(interview.getPercentage())
        );

        double totalScore = scores.stream().mapToDouble(Double::doubleValue).sum();
        int averageScore = scores.isEmpty()
                ? 0
                : (int) Math.round(totalScore / scores.size());
        double bestScore = scores.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);

        return new ProgressSummaryResponse(
                activitiesCompleted,
                questionsAttempted,
                averageScore,
                bestScore,
                calculateCurrentStreak(activeDates)
        );
    }

    private boolean isQualifyingInterview(String interviewType) {
        if (interviewType == null) {
            return false;
        }

        String normalized = interviewType.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z]", "");
        return normalized.startsWith("technical") || normalized.startsWith("hr");
    }

    private void addActiveDate(Set<LocalDate> activeDates, LocalDateTime timestamp) {
        if (timestamp != null) {
            activeDates.add(timestamp.atZone(APPLICATION_ZONE).toLocalDate());
        }
    }

    private int calculateCurrentStreak(Set<LocalDate> activeDates) {
        LocalDate today = LocalDate.now(APPLICATION_ZONE);
        LocalDate streakDay = activeDates.contains(today) ? today : today.minusDays(1);

        if (!activeDates.contains(streakDay)) {
            return 0;
        }

        int streak = 0;
        while (activeDates.contains(streakDay)) {
            streak++;
            streakDay = streakDay.minusDays(1);
        }
        return streak;
    }
}
