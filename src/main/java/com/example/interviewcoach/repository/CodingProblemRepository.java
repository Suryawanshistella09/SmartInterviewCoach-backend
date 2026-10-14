package com.example.interviewcoach.repository;

import com.example.interviewcoach.entity.CodingProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingProblemRepository
        extends JpaRepository<CodingProblem, Long> {

    List<CodingProblem> findBySubjectAndDifficulty(
            String subject,
            String difficulty
    );

    List<CodingProblem> findBySubject(String subject);
}
