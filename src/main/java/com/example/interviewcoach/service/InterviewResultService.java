package com.example.interviewcoach.service;

import com.example.interviewcoach.entity.InterviewResult;
import com.example.interviewcoach.repository.InterviewResultRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class InterviewResultService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Kolkata");

    private final InterviewResultRepository interviewResultRepository;

    public InterviewResultService(
            InterviewResultRepository interviewResultRepository) {

        this.interviewResultRepository = interviewResultRepository;
    }


    // =====================================================
    // SAVE INTERVIEW RESULT
    // =====================================================

    public InterviewResult saveResult(InterviewResult result) {

        result.setCompletedAt(LocalDateTime.now(APPLICATION_ZONE));

        return interviewResultRepository.save(result);
    }


    // =====================================================
    // GET RESULTS OF A PARTICULAR STUDENT
    // =====================================================

    public List<InterviewResult> getResultsByUser(String userEmail) {

        return interviewResultRepository.findByUserEmail(userEmail);
    }


    // =====================================================
    // GET ALL INTERVIEW RESULTS
    // =====================================================

    public List<InterviewResult> getAllResults() {

        return interviewResultRepository.findAll();
    }


    // =====================================================
    // DASHBOARD STATISTICS
    // =====================================================

    public Map<String, Object> getDashboardStats(String userEmail) {

        List<InterviewResult> results =
                interviewResultRepository.findByUserEmail(userEmail);


        // -------------------------------------------------
        // INTERVIEWS COMPLETED
        // -------------------------------------------------

        int interviewsCompleted =
                results.size();


        // -------------------------------------------------
        // QUESTIONS ATTEMPTED
        // -------------------------------------------------

        int questionsAttempted =
                results.stream()
                        .mapToInt(result ->
                                result.getTotalQuestions()
                        )
                        .sum();


        // -------------------------------------------------
        // AVERAGE SCORE
        // -------------------------------------------------

        double averageScore = 0;

        if (!results.isEmpty()) {

            double totalPercentage =
                    results.stream()
                            .mapToDouble(result ->
                                    result.getPercentage()
                            )
                            .sum();

            averageScore =
                    totalPercentage / results.size();
        }


        // Round average score
        averageScore =
                Math.round(averageScore);


        // -------------------------------------------------
        // CURRENT STREAK
        // -------------------------------------------------

        int currentStreak =
                calculateCurrentStreak(results);


        // -------------------------------------------------
        // RESPONSE
        // -------------------------------------------------

        Map<String, Object> stats =
                new HashMap<>();

        stats.put(
                "interviewsCompleted",
                interviewsCompleted
        );

        stats.put(
                "questionsAttempted",
                questionsAttempted
        );

        stats.put(
                "averageScore",
                averageScore
        );

        stats.put(
                "currentStreak",
                currentStreak
        );


        return stats;
    }


    // =====================================================
    // CALCULATE CURRENT STREAK
    // =====================================================

    private int calculateCurrentStreak(
            List<InterviewResult> results) {

        if (results.isEmpty()) {
            return 0;
        }


        // Get unique interview dates
        Set<LocalDate> uniqueDates =
                new HashSet<>();


        for (InterviewResult result : results) {

            if (result.getCompletedAt() != null) {

                uniqueDates.add(
                        result.getCompletedAt()
                                .toLocalDate()
                );
            }
        }


        if (uniqueDates.isEmpty()) {
            return 0;
        }


        // Sort dates from newest to oldest
        List<LocalDate> dates =
                new ArrayList<>(uniqueDates);

        dates.sort(
                Comparator.reverseOrder()
        );


        // -------------------------------------------------
        // Check whether latest interview was today
        // or yesterday
        // -------------------------------------------------

        LocalDate today =
                LocalDate.now();

        LocalDate latestDate =
                dates.get(0);


        long daysFromToday =
                ChronoUnit.DAYS.between(
                        latestDate,
                        today
                );


        if (daysFromToday > 1) {
            return 0;
        }


        // -------------------------------------------------
        // Calculate consecutive days
        // -------------------------------------------------

        int streak = 1;


        for (int i = 0; i < dates.size() - 1; i++) {

            LocalDate current =
                    dates.get(i);

            LocalDate previous =
                    dates.get(i + 1);


            long difference =
                    ChronoUnit.DAYS.between(
                            previous,
                            current
                    );


            if (difference == 1) {

                streak++;

            } else {

                break;
            }
        }


        return streak;
    }
}
