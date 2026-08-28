package com.pdd.pddai.controller;

import com.pdd.pddai.dto.AiAnalysisTicketRequestDto;
import com.pdd.pddai.dto.UserStatisticsDto;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.UserRepository;
import com.pdd.pddai.service.AgentService;
import com.pdd.pddai.service.PromptBuilderService;
import com.pdd.pddai.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AgentService agentService;
    private final PromptBuilderService promptBuilderService;
    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

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
    @GetMapping("/analyze-statistics")
    public ResponseEntity<String> analyzeStatistics() {
        try {
            // 1. Получаем telegramId из JWT
            String telegramId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 2. Находим пользователя
            UserEntity user = userRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // 3. Получаем статистику (UserStatisticsDto)
            UserStatisticsDto stats = statisticsService.getUserStatistics(user.getId());

            // 4. Формируем промт
            String prompt = promptBuilderService.buildPromptByStatistics(stats);

            // 5. Отправляем в AI
            String analysis = agentService.askAgent(prompt);

            // 6. Возвращаем результат
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }
    }

}
