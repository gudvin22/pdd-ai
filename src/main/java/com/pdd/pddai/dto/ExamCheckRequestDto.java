package com.pdd.pddai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExamCheckRequestDto {
    private int ticketNumber;
    private List<Integer> answers;
}
