package com.pdd.pddai.dto;

import lombok.Data;

import java.util.List;


@Data
public class QuestionDto {

    private int ticketNumber;
    private int questionNumber;
    private int rightAnswerNumber;
    private String questionText;
    private List<String> answersText;
    private String imageUrlSmall;
    private String imageUrlBig;
}
