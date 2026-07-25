package com.pdd.pddai.controller;

import com.pdd.pddai.service.ExplanationGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExplanationGeneratorService explanationGeneratorService;

    @PostMapping("/generate-explanations")
    public ResponseEntity<String> generateExplanations() {
        try {
            explanationGeneratorService.generateAllExplanations();
            return ResponseEntity.ok("Генерация объяснений запущена. Проверьте логи.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }

    }
}
