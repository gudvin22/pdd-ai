package com.pdd.pddai.service;

import com.pdd.pddai.dto.AiAnalysisTicketRequestDto;
import com.pdd.pddai.dto.UserStatisticsDto;
import com.pdd.pddai.dto.WeakTopicDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptBuilderService {
    private final QuestionRepository questionRepository;

    public String buildPromptByErrorTicket(AiAnalysisTicketRequestDto aiAnalysisTicketRequestDto) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Пользователь сдавал билет №").append(aiAnalysisTicketRequestDto.getTicketNumber())
                .append(" и допустил следующие ошибки:\n\n");

        List<QuestionEntity> questions = questionRepository.findByTicketNumberOrderByQuestionNumberAsc(aiAnalysisTicketRequestDto.getTicketNumber());
        if (questions.isEmpty()) {
            throw new TicketNotFoundException("Билета с номером " +aiAnalysisTicketRequestDto.getTicketNumber() +" не существует");
        }

        Map<Integer,QuestionEntity> questionMap = questions.stream()
                .collect(Collectors.toMap(QuestionEntity::getQuestionNumber, questionEntity -> questionEntity));


        //идем по ошибкам
        for (AiAnalysisTicketRequestDto.ErrorDetail error : aiAnalysisTicketRequestDto.getErrors()) {

             QuestionEntity question = questionMap.get(error.getQuestionNumber());
             if (question == null) {
                 log.error("не получилось получить вопрос из БД");
                 continue;
             }

             String questionText = question.getQuestionText();
             List<String> answers = question.getAnswersText();
             int correctAnswerIndex = error.getCorrectAnswerIndex();
             int userAnswerIndex = error.getUserAnswerIndex();
             String topic = question.getTopic() != null ? question
                     .getTopic().getTopicName() : "Без темы";

            prompt.append("Ошибка в вопросе ").append(error.getQuestionNumber())
                    .append(" (Тема: ").append(topic).append(")\n");
            prompt.append("Вопрос: ").append(questionText).append("\n");
            prompt.append("Варианты ответов:\n");
            for (int i = 0; i < answers.size(); i++) {
                prompt.append("  ").append(i + 1).append(". ").append(answers.get(i)).append("\n");
            }
            prompt.append("Пользователь выбрал: ")
                    .append(userAnswerIndex + 1).append("\n");
            prompt.append("Правильный ответ: ")
                    .append(correctAnswerIndex + 1).append("\n");

        }

        prompt.append("Проанализируй ошибки. Дай общую аналитику (не по каждому вопросу): \n" +
                "- выдели слабые темы,\n" +
                "- дай краткие рекомендации по их изучению,\n" +
                "- оцени готовность к экзамену скажи, сколько примерно времени потребуется на подготовку к экзамену, мотивируй пользователя.\n" +
                "Ответ должен быть кратким (до 500 токенов) и структурированным.\n");

        return prompt.toString();
    }

    public String buildPromptByStatistics(UserStatisticsDto stats) {
        StringBuilder prompt = new StringBuilder();

        int totalAnswers = stats.getTotalAttempts() * 20;
        int correctAnswers = totalAnswers - stats.getTotalWrong();
        int accuracy = totalAnswers == 0 ? 0 : (correctAnswers * 100) / totalAnswers;
        int mastered = stats.getCorrectTickets();
        int weakCount = stats.getWeakTopics() != null ? stats.getWeakTopics().size() : 0;
        String topWeakTopic = stats.getWeakTopics() != null && !stats.getWeakTopics().isEmpty()
                ? stats.getWeakTopics().get(0).getTopicName()
                : "";

        prompt.append("Ты — преподаватель ПДД. Дай ученику анализ и рекомендации по его статистике.\n\n");

        prompt.append("Статистика:\n");
        prompt.append("- Точность: ").append(accuracy).append("%\n");
        prompt.append("- Билетов без ошибок: ").append(mastered).append(" из 40\n");
        prompt.append("- Количество слабых тем: ").append(weakCount).append("\n");
        prompt.append("- Самая слабая тема: ").append(topWeakTopic).append("\n\n");

        prompt.append("Напиши ответ строго по структуре:\n");
        prompt.append("1. Твоя точность — в процентах %. сколько надо времени уделять в день ?.\n");
        prompt.append("2. Я подготовил для тебя задания именно по твоим слабым темам их несколько.\n");
        prompt.append("3. Перейди в раздел «Рекомендация» в меню бота, чтобы начать тренировку.\n");
        prompt.append("4. Сколько примерно надо времени для сдачи экзамена бех ошибок проанализируй.\n\n");

        prompt.append("Не пиши общих фраз. Не предлагай решать билеты. Не пиши про сайт — только про раздел в меню бота.\n");
        prompt.append("Ответ — 8-10 предложений.\n");
        prompt.append("В конце добавь: «Текст ПДД РФ: https://www.consultant.ru/document/cons_doc_LAW_2709/».");

        return prompt.toString();
    }


}
