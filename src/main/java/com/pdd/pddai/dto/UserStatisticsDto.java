package com.pdd.pddai.dto;

import com.pdd.pddai.dto.WeakTopicDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {
    private Long userId;
    private int totalAttempts;
    private int correctTickets;
    private int incorrectTickets;
    private int totalWrong;
    private LocalDateTime lastAttemptDate;
    private List<WeakTopicDto> weakTopics;

    public static UserStatisticsDto empty(Long userId) {
        return UserStatisticsDto.builder()
                .userId(userId)
                .totalAttempts(0)
                .correctTickets(0)
                .incorrectTickets(0)
                .totalWrong(0)
                .lastAttemptDate(null)
                .weakTopics(List.of())
                .build();
    }
}