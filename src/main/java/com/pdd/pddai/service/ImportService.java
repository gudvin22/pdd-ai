package com.pdd.pddai.service;

import com.pdd.pddai.config.ImportProperties;
import com.pdd.pddai.dto.QuestionDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final ImportProperties importProperties;

    public void importQuestions() throws Exception {
        System.out.println("Вызов метода импорта вопросов");
        // путь к json
        String filePath = importProperties.getFile();
        //загрузить ресурс и открыть поток
        var resource = resourceLoader.getResource(filePath);

        try (var inputStream = resource.getInputStream()) {

            // читаем json  в dto
            var typeFactory = objectMapper.getTypeFactory();
            var listType = typeFactory.constructCollectionType(List.class, QuestionDto.class);
            List<QuestionDto> dtos = objectMapper.readValue(inputStream, listType);

            System.out.println("Всего прочитано DTO: " + dtos.size());

            // проходим по каждому dto сохраняем в entity
            int saveCount = 0;
            for(QuestionDto dto : dtos) {
                QuestionEntity entity = new QuestionEntity();
                entity.setCategory(dto.getCategory());
                entity.setTicketNumber(dto.getTicketNumber());
                entity.setQuestionNumber(dto.getQuestionNumber());
                entity.setQuestionText(dto.getQuestionText());
                entity.setCorrectAnswerIndex(dto.getRightAnswerNumber());
                entity.setAnswersText(dto.getAnswersText());
                entity.setImageUrlSmall("0".equals(dto.getImageUrlSmall()) ? null : dto.getImageUrlSmall());
                entity.setImageUrlBig("0".equals(dto.getImageUrlBig()) ? null : dto.getImageUrlBig());


                if (!questionRepository.existsByCategoryAndTicketNumberAndQuestionNumber(
                        dto.getCategory(), dto.getTicketNumber(), dto.getQuestionNumber())) {
                    questionRepository.save(entity);
                    saveCount++;
                } else {
                    System.out.println("Пропущен дубликат: " + dto.getCategory() + "/" + dto.getTicketNumber() + "/" + dto.getQuestionNumber());
                }

            }

            System.out.println("сохранено вопросов: " + saveCount);

        }

    }
}
