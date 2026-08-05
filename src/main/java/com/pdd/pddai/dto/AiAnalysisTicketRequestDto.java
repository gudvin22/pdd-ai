package com.pdd.pddai.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiAnalysisTicketRequestDto {
    private int ticketNumber;
    private List<ErrorDetail> errors;

    @Data
    public static class ErrorDetail {
        private int questionNumber;
        private int userAnswerIndex;
        private int correctAnswerIndex;
    }

}
