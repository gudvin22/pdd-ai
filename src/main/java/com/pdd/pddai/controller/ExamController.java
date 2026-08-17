package com.pdd.pddai.controller;

import com.pdd.pddai.dto.ExamCheckRequestDto;
import com.pdd.pddai.dto.ExamResponseDto;
import com.pdd.pddai.dto.QuestionResponseDto;
import com.pdd.pddai.dto.WrongAnswerDto;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.service.ExamService;
import com.pdd.pddai.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;
    private final StatisticsService statisticsService;

    @GetMapping("/random")
    public ResponseEntity<ExamResponseDto> getRandomTicket() {
        int randomTicket = (int) (Math.random() * 40) + 1;

        try {
            List<QuestionResponseDto> questions = examService.getTicketNumber(randomTicket);
            ExamResponseDto examResponseDto = new ExamResponseDto();
            examResponseDto.setQuestions(questions);
            examResponseDto.setTicketNumber(randomTicket);
            return ResponseEntity.ok(examResponseDto);

        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/check")
    public ResponseEntity<List<WrongAnswerDto>> checkTicket(@RequestBody ExamCheckRequestDto examCheckRequestDto) {
        try {
            // 1. Проверяем билет и получаем список ошибок
            List<WrongAnswerDto> wrongAnswers = examService.checkExam(examCheckRequestDto);

            // 2. Сохраняем статистику (если аутентификация пройдена)
            try {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof String) {
                    String telegramId = (String) principal;
                    statisticsService.collectAndSaveAttempt(telegramId, examCheckRequestDto.getTicketNumber(), wrongAnswers);
                }
            } catch (Exception e) {
                log.error("Ошибка при сохранении статистики: {}", e.getMessage(), e);
            }

            // 3. Возвращаем результат клиенту
            return ResponseEntity.ok(wrongAnswers);

        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}