package com.pdd.pddai.service;

import com.pdd.pddai.dto.TicketStatusDto;
import com.pdd.pddai.dto.UserStatisticsDto;
import com.pdd.pddai.dto.WeakTopicDto;
import com.pdd.pddai.dto.WrongAnswerDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.UserAttemptsEntity;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.TicketStatus;
import com.pdd.pddai.repository.QuestionRepository;
import com.pdd.pddai.repository.UserAttemptsRepository;
import com.pdd.pddai.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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

    public List<TicketStatusDto> getTicketStatuses(Long userId) {
        List<UserAttemptsEntity> attempts = userAttemptsRepository.findByUser_IdOrderByAttemptDateDesc(userId);

        //Группируем по номеру билета, оставляя последнюю попытку (первую в списке)
        Map<Integer, UserAttemptsEntity> lastAttemptByTicket = attempts.stream()
                .collect(Collectors.toMap(
                        UserAttemptsEntity::getTicketNumber,
                        Function.identity(),
                        (existing, replacement) -> existing // так как список уже отсортирован, первый элемент – самый новый
                ));

        List<TicketStatusDto> result = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            UserAttemptsEntity attempt = lastAttemptByTicket.get(i);
            TicketStatus status;
            if (attempt == null) {
                status = TicketStatus.NOT_ATTEMPTED;
            } else if (attempt.getWrongCount() == 0) {
                status = TicketStatus.CORRECT;
            } else {
                status = TicketStatus.INCORRECT;
            }
            result.add(new TicketStatusDto(i, status));
        }
        return result;
        }

    public UserStatisticsDto getUserStatistics(Long userId) {
        List<UserAttemptsEntity> attempts = userAttemptsRepository.findByUser_IdOrderByAttemptDateDesc(userId);

        if (attempts.isEmpty()) {
            return UserStatisticsDto.empty(userId);
        }

        int totalAttempts = attempts.size();
        int totalWrongCount = attempts.stream().mapToInt(UserAttemptsEntity::getWrongCount).sum();

        Map<Integer, UserAttemptsEntity> lastAttemptByTicket = new HashMap<>();
        for(UserAttemptsEntity attempt : attempts) {
            int ticketNumber = attempt.getTicketNumber();
            UserAttemptsEntity lastAttempt = lastAttemptByTicket.get(ticketNumber);
            if (lastAttempt == null || attempt.getAttemptDate().isAfter(lastAttempt.getAttemptDate())) {
                lastAttemptByTicket.put(ticketNumber, attempt);
            }
        }

        int correctTickets = 0;
        int incorrectTickets = 0;

        for (UserAttemptsEntity attempt : lastAttemptByTicket.values()) {
            if (attempt.getWrongCount() == 0) {
                correctTickets++;
            } else {
                incorrectTickets++;
            }
        }

        LocalDateTime lastAttemptDate = attempts.stream()
                .map(UserAttemptsEntity::getAttemptDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return UserStatisticsDto.builder()
                .userId(userId)
                .totalAttempts(totalAttempts)
                .correctTickets(correctTickets)
                .incorrectTickets(incorrectTickets)
                .totalWrong(totalWrongCount)
                .lastAttemptDate(lastAttemptDate)
                .weakTopics(getWeakTopics(attempts, 5)) // пока пустой список
                .build();
    }

    private List<WeakTopicDto> getWeakTopics(List<UserAttemptsEntity> attempts, int limit) {

        Map<Long, Integer> topicErrorCount = new HashMap<>();

        for (UserAttemptsEntity attempt : attempts) {
            if (attempt.getWrongTopicIds() != null) {

                for (Integer topicId : attempt.getWrongTopicIds()) {

                    topicErrorCount.put(topicId.longValue(),
                            topicErrorCount.getOrDefault(topicId.longValue(), 0) + 1);
                }
            }
        }

        return topicErrorCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    String topicName = questionRepository.findTopicNameById(entry.getKey())
                            .orElse(null);
                    if (topicName == null) {
                        return null; // если тема не найдена — пропускаем
                    }
                    return new WeakTopicDto(entry.getKey(), topicName, entry.getValue());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }



}
