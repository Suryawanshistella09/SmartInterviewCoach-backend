package com.example.interviewcoach.repository;

import com.example.interviewcoach.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository
        extends JpaRepository<TestCase, Long> {

    List<TestCase> findByCodingProblem_Id(Long codingProblemId);

    List<TestCase> findByCodingProblem_IdAndHiddenFalse(Long codingProblemId);

    List<TestCase> findByCodingProblem_IdOrderByIdAsc(Long codingProblemId);

    List<TestCase> findByCodingProblem_IdAndHiddenFalseOrderByIdAsc(Long codingProblemId);
}
