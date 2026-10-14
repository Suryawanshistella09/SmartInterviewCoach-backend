package com.example.interviewcoach.service;

import com.example.interviewcoach.repository.InterviewResultRepository;
import com.example.interviewcoach.repository.CodingProblemRepository;
import com.example.interviewcoach.repository.QuestionRepository;
import com.example.interviewcoach.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CodingProblemRepository codingProblemRepository;
    private final InterviewResultRepository interviewResultRepository;

    public AdminStatsService(
            UserRepository userRepository,
            QuestionRepository questionRepository,
            CodingProblemRepository codingProblemRepository,
            InterviewResultRepository interviewResultRepository) {

        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.codingProblemRepository = codingProblemRepository;
        this.interviewResultRepository = interviewResultRepository;
    }


    // =====================================================
    // GET ADMIN DASHBOARD STATISTICS
    // =====================================================

    public Map<String, Object> getAdminStats() {

        // -------------------------------------------------
        // TOTAL USERS
        // -------------------------------------------------

        long totalUsers = userRepository.count();


        // -------------------------------------------------
        // TOTAL QUESTIONS
        // -------------------------------------------------

        long totalQuestions =
                questionRepository.count() + codingProblemRepository.count();


        // -------------------------------------------------
        // TOTAL INTERVIEWS
        // -------------------------------------------------

        long totalInterviews = interviewResultRepository.count();


        // -------------------------------------------------
        // AVERAGE SCORE (across all results)
        // -------------------------------------------------

        double averageScore = 0;

        if (totalInterviews > 0) {

            double totalPercentage =
                    interviewResultRepository.findAll()
                            .stream()
                            .mapToDouble(r -> r.getPercentage())
                            .sum();

            averageScore =
                    Math.round(
                            (totalPercentage / totalInterviews) * 10.0
                    ) / 10.0;
        }


        // -------------------------------------------------
        // PER-TYPE INTERVIEW COUNTS
        // -------------------------------------------------

        long technicalCount =
                interviewResultRepository.countByInterviewType("Technical");

        long codingCount =
                interviewResultRepository.countByInterviewType("Coding");

        long hrCount =
                interviewResultRepository.countByInterviewType("HR");


        // -------------------------------------------------
        // BUILD RESPONSE MAP
        // -------------------------------------------------

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers",      totalUsers);
        stats.put("totalQuestions",  totalQuestions);
        stats.put("totalInterviews", totalInterviews);
        stats.put("averageScore",    averageScore);
        stats.put("technicalCount",  technicalCount);
        stats.put("codingCount",     codingCount);
        stats.put("hrCount",         hrCount);

        return stats;
    }
}
