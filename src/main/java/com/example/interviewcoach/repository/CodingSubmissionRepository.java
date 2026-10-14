package com.example.interviewcoach.repository;

import com.example.interviewcoach.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingSubmissionRepository
        extends JpaRepository<CodingSubmission, Long> {

    List<CodingSubmission> findByUserEmail(String userEmail);

    List<CodingSubmission> findByUserEmailOrderBySubmittedAtDescIdDesc(
            String userEmail
    );

    List<CodingSubmission> findByUserEmailAndCodingProblemId(
            String userEmail,
            Long codingProblemId
    );
}
