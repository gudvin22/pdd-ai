package com.pdd.pddai.controller;

import com.pdd.pddai.dto.TelegramRegistrationDto;
import com.pdd.pddai.exception.UserAlreadyExistsException;
import com.pdd.pddai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/telegram-register")
    public ResponseEntity<String> registerTelegram(@RequestBody TelegramRegistrationDto telegramRegistrationDto) {
        try {
            String token = authService.registerTelegramUser(telegramRegistrationDto);
            return ResponseEntity.ok(token);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }

    }


}
