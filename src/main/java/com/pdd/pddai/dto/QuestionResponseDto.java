package com.pdd.pddai.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionResponseDto {
    private int questionNumber;
    private String questionText;
    private List<String> answersText;
    private String imageUrlSmall;
    private int correctAnswerIndex;

}
