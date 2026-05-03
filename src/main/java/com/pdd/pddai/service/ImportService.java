package com.pdd.pddai.service;

import com.pdd.pddai.config.ImportProperties;
import com.pdd.pddai.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final ImportProperties importProperties;

    public void importQuestions() throws Exception {
        System.out.println("Вызов метода импорта вопросов");
    }
}
