package com.example.interviewcoach.repository;

import com.example.interviewcoach.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
