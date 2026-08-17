package com.pdd.pddai.service;

import com.pdd.pddai.dto.WrongAnswerDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.UserAttemptsEntity;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.repository.QuestionRepository;
import com.pdd.pddai.repository.UserAttemptsRepository;
import com.pdd.pddai.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final UserAttemptsRepository userAttemptsRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;


    private void saveAttempt(UserEntity user, Integer ticketNumber, Integer wrongCount, List<Integer> wrongTopicIds) {
        UserAttemptsEntity attempt = new UserAttemptsEntity();
        attempt.setUser(user);
        attempt.setTicketNumber(ticketNumber);
        attempt.setWrongCount(wrongCount);
        attempt.setWrongTopicIds(wrongTopicIds);
        userAttemptsRepository.save(attempt);
    }

    @Transactional
    public void collectAndSaveAttempt(String telegramId, int ticketNumber, List<WrongAnswerDto> wrongAnswers) {
        // Находим пользователя
        UserEntity user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Загрузка вопросов
        List<QuestionEntity> questions = questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);

        Map<Integer,QuestionEntity> questionMap  = questions.stream()
                .collect(Collectors.toMap(QuestionEntity::getQuestionNumber, q -> q));

        Set<Long> topicIdSet = wrongAnswers.stream()
                .map(w -> questionMap.get(w.getQuestionNumber()))
                .filter(Objects::nonNull)           // если вопрос не найден – пропускаем
                .map(q -> q.getTopic().getId())
                .filter(Objects::nonNull)           // если тема не задана – пропускаем
                .collect(Collectors.toSet());

        int wrongCount = wrongAnswers.size();

        List<Integer> topicIds = topicIdSet.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        saveAttempt(user, ticketNumber, wrongCount, topicIds);


    }
}
