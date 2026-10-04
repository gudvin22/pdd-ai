package com.pdd.pddai.controller;

import com.pdd.pddai.dto.SubscriptionDto;
import com.pdd.pddai.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/info")
    public ResponseEntity<SubscriptionDto> getInfo() {
        try {
            String telegramId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(subscriptionService.getSubscriptionInfo(telegramId));
        } catch (Exception e) {
            log.error("Ошибка получения информации о подписке: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<SubscriptionDto> activate() {
        try {
            String telegramId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            SubscriptionDto result = subscriptionService.activateSubscription(telegramId, 1);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка активации подписки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionDto> cancel() {
        try {
            String telegramId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(subscriptionService.cancelSubscription(telegramId));
        } catch (Exception e) {
            log.error("Ошибка отмены подписки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}