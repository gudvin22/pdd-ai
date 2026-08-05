package com.pdd.pddai.dto;

import lombok.Data;

@Data
public class WrongAnswerDto {
    private int ticketNumber;
    private int questionNumber;
    private int correctAnswerIndex;
    private int userAnswerIndex;
    private String explanation;
    private String topicName;


}
