package com.example.interviewcoach.repository;

import com.example.interviewcoach.entity.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewResultRepository
        extends JpaRepository<InterviewResult, Long> {

    List<InterviewResult> findByUserEmail(String userEmail);
}