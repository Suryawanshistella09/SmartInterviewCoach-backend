package com.example.interviewcoach.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coding_problems")
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String difficulty;

    private String title;

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String inputFormat;

    @Column(columnDefinition = "TEXT")
    private String outputFormat;

    @Column(columnDefinition = "TEXT")
    private String examples;

    @Column(columnDefinition = "TEXT")
    private String constraints;

    // Starter code templates per language

    @Column(columnDefinition = "TEXT")
    private String starterJava;

    @Column(columnDefinition = "TEXT")
    private String starterPython;

    @Column(columnDefinition = "TEXT")
    private String starterC;

    @Column(columnDefinition = "TEXT")
    private String starterCpp;

    @OneToMany(mappedBy = "codingProblem")
    private List<TestCase> testCases = new ArrayList<>();


    // Default constructor
    public CodingProblem() {
    }


    // Getters and Setters

    public Long getId() {
        return id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getStarterJava() {
        return starterJava;
    }

    public void setStarterJava(String starterJava) {
        this.starterJava = starterJava;
    }

    public String getStarterPython() {
        return starterPython;
    }

    public void setStarterPython(String starterPython) {
        this.starterPython = starterPython;
    }

    public String getStarterC() {
        return starterC;
    }

    public void setStarterC(String starterC) {
        this.starterC = starterC;
    }

    public String getStarterCpp() {
        return starterCpp;
    }

    public void setStarterCpp(String starterCpp) {
        this.starterCpp = starterCpp;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }
}
