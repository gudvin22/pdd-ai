package com.pdd.pddai.controller;

import com.pdd.pddai.dto.TicketStatusDto;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.repository.UserRepository;
import com.pdd.pddai.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

    @GetMapping("/tickets-status")
    public ResponseEntity<List<TicketStatusDto>> getTicketsStatus() {
        try {
            String telegramId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserEntity user = userRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            return ResponseEntity.ok(statisticsService.getTicketStatuses(user.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}