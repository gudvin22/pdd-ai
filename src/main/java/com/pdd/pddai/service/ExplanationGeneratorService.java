package com.pdd.pddai.service;

import com.pdd.pddai.dto.ExamCheckRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExplanationGeneratorService {
    private final AgentService agentService;

    public void importQuestions() throws Exception {
        System.out.println(agentService.askAgent("я хочу пива? ответь не больше 300 токунов"));
    }
}
