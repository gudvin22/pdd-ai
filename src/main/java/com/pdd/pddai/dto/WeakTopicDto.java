package com.pdd.pddai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeakTopicDto {
    private  Long topicId;
    private  String topicName;
    private int wrongCount;
}
