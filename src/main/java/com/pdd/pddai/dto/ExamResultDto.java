package com.pdd.pddai.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamResultDto {
    private boolean passed;
    private String message;
    private List<WrongAnswerDto> wrongAnswers;
    private List<QuestionResponseDto> additionalQuestions; // для дополнительных вопросов
}