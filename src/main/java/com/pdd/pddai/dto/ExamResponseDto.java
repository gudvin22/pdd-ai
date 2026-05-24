package com.pdd.pddai.dto;

import com.pdd.pddai.entity.QuestionEntity;
import lombok.Data;

import java.util.List;

@Data
public class ExamResponseDto {
    private int ticketNumber;
    private List<QuestionResponseDto> questions;
}
