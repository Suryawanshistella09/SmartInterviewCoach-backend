package com.example.interviewcoach.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_results")
public class InterviewResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String interviewType;

    private String subject;

    private String difficulty;

    private int totalQuestions;

    private int correctAnswers;

    private int partialAnswers;

    private int wrongAnswers;

    private int score;

    private double percentage;

    private LocalDateTime completedAt;


    // Default constructor
    public InterviewResult() {
    }


    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getPartialAnswers() {
        return partialAnswers;
    }

    public void setPartialAnswers(int partialAnswers) {
        this.partialAnswers = partialAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}