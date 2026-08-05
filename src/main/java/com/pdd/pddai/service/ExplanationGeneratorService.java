package com.pdd.pddai.service;


import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExplanationGeneratorService {
    private final AgentService agentService;
    private final QuestionRepository questionRepository;

    @Transactional
    public void generateAllExplanations() throws Exception {

        List<QuestionEntity> questions = questionRepository.findByQuestHelpIsNull();
        int total = questions.size();

        log.info("Найдено вопросов без объяснений: {}", total);

        int success = 0;
        int fail = 0;

        // Проходим по каждому вопросу
        for (int i = 0; i <= 1; i++) {
            QuestionEntity question = questions.get(i);

            log.info("Обработка {}/{}: билет {}, вопрос {}",
                    i + 1, total, question.getTicketNumber(), question.getQuestionNumber());

            try {
                String prompt = buildPrompt(question);

                String explanation = agentService.askAgent(prompt);

                // Если ответ получен и не содержит сообщение об ошибке пишем в базу

                if (explanation != null && !explanation.startsWith("Не удалось получить")) {

                    question.setQuestHelp(explanation);
                    questionRepository.save(question);
                    success++;
                    log.info("✅ Объяснение сохранено для билета {}, вопроса {}",
                            question.getTicketNumber(), question.getQuestionNumber());
                } else {
                    // Если ответ пустой или ошибочный – логируем предупреждение
                    log.warn("⚠️ Агент вернул пустой ответ для билета {}, вопроса {}",
                            question.getTicketNumber(), question.getQuestionNumber());
                    fail++;

                    Thread.sleep(1500);

                }
            } catch (Exception e) {
                log.error("❌ Ошибка при генерации для билета {}, вопроса {}: {}",
                        question.getTicketNumber(), question.getQuestionNumber(), e.getMessage());
                fail++;
            }

            log.info("✅ Генерация завершена. Успешно: {}, Ошибок: {}", success, fail);
        }
    }

    private String buildPrompt(QuestionEntity question) {

        String correctAnswer = question.getAnswersText().get(question.getCorrectAnswerIndex());

        StringBuilder sb = new StringBuilder();
        sb.append("Вопрос: ").append(question.getQuestionText()).append("\n");
        sb.append("Варианты:\n");
        // Добавляем все варианты с нумерацией
        for (int i = 0; i < question.getAnswersText().size(); i++) {
            sb.append(i + 1).append(". ").append(question.getAnswersText().get(i)).append("\n");
        }
        sb.append("Правильный ответ: ").append(correctAnswer).append("\n");
        sb.append("Объясни, напиши комментарий к ответу почему он правильный. не надо здороваться. Дай чёткое, краткое объяснение для ученика автошколы. Не забывай что ты опытный инструктор и преподаватель ПДД. ");

        return sb.toString();
    }

}
