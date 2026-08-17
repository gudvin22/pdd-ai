package com.pdd.pddai.service;

import com.pdd.pddai.dto.AiAnalysisTicketRequestDto;
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

        prompt.append("Проанализируй эти ошибки. Дай общую аналитику (не по каждому вопросу отдельно): \n" +
                "- выдели слабые темы,\n" +
                "- дай краткие рекомендации по их изучению,\n" +
                "- оцени готовность к экзамену в процентах,\n" +
                "- мотивируй пользователя и скажи, сколько примерно времени потребуется на повторение.\n" +
                "Ответ должен быть кратким (до 500 токенов) и структурированным.\n");

        return prompt.toString();
    }

}
