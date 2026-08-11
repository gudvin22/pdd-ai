package com.pdd.pddai.controller;

import com.pdd.pddai.dto.AiAnalysisTicketRequestDto;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.service.AgentService;
import com.pdd.pddai.service.PromptBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AgentService agentService;
    private final PromptBuilderService promptBuilderService;

    @PostMapping("/analyze-errors")
    public ResponseEntity<?> analyzeError(@RequestBody AiAnalysisTicketRequestDto aiAnalysisTicketRequestDto) {

        try {
            String prompt = promptBuilderService.buildPromptByErrorTicket(aiAnalysisTicketRequestDto);
            String answerAi = agentService.askAgent(prompt);
            return ResponseEntity.ok(answerAi);
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Билет не найден");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при генерации анализа: " + e.getMessage());
        }

    }
}
