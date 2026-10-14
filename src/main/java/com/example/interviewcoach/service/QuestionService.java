package com.example.interviewcoach.service;

import com.example.interviewcoach.entity.Question;
import com.example.interviewcoach.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Question addQuestion(Question question) {
        return questionRepository.save(question);
    }

    public Question updateQuestion(Long id, Question question) {

        Question existingQuestion =
                questionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Question not found"));

        existingQuestion.setInterviewType(question.getInterviewType());
        existingQuestion.setSubject(question.getSubject());
        existingQuestion.setDifficulty(question.getDifficulty());
        existingQuestion.setQuestion(question.getQuestion());
        existingQuestion.setTopic(question.getTopic());
        existingQuestion.setKeywords(question.getKeywords());

        return questionRepository.save(existingQuestion);
    }

    public String deleteQuestion(Long id) {

        if (!questionRepository.existsById(id)) {
            return "Question not found";
        }

        questionRepository.deleteById(id);

        return "Question deleted successfully";
    }
}
