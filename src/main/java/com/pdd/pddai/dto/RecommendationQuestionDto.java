package com.pdd.pddai.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendationQuestionDto {
    private long questionId;
    private String questionText;
    private List<String> answers;
    private int correctAnswerIndex;
    private String explanation;
    private String imageUrlSmall;
}
