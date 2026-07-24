package com.pdd.pddai.controller;

import com.pdd.pddai.service.ExplanationGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class AdminController {

    private final ExplanationGeneratorService explanationGeneratorService;

    @PostMapping("/agent")
    public String testAgent() throws Exception {
        explanationGeneratorService.importQuestions();
        return "Метод выполнен. Проверьте консоль.";
    }
}
