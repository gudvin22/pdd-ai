package com.pdd.pddai.controller;

import com.pdd.pddai.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;

    @PostMapping("/questions")
    public String importQuestions() {
        try {
            importService.importQuestions();
            return "Импорт успешно завершён";
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка импорта: " + e.getMessage();
        }
    }
}
