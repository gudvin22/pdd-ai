package com.pdd.pddai.service;

import com.pdd.pddai.dto.*;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.UserAttemptsEntity;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.QuestionRepository;
import com.pdd.pddai.repository.UserAttemptsRepository;
import com.pdd.pddai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserAttemptsRepository userAttemptsRepository;
    private final StatisticsService statisticsService;



    public List<QuestionResponseDto> getTicketNumber (int ticketNumber) {

        List<QuestionEntity> questions = questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);
        if (questions.isEmpty()) {
            throw new TicketNotFoundException("Билета с номером " +ticketNumber +" не существует");
        }

        return questions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private QuestionResponseDto convertToDto(QuestionEntity entity) {
        QuestionResponseDto dto = new QuestionResponseDto();
        dto.setQuestionNumber(entity.getQuestionNumber());
        dto.setQuestionText(entity.getQuestionText());
        dto.setAnswersText(entity.getAnswersText());
        dto.setImageUrlSmall(entity.getImageUrlSmall());
        return dto;
    }

    public ExamResultDto checkExamStrict(ExamCheckRequestDto request) {
        ExamResultDto result = new ExamResultDto();
        return result;


    }


    public List<WrongAnswerDto> checkExam(ExamCheckRequestDto request) {

        List<QuestionEntity> questions = questionRepository.findByTicketNumberOrderByQuestionNumberAsc(request.getTicketNumber());
        if (questions.isEmpty()) {
            throw new TicketNotFoundException("Билета с номером " +request.getTicketNumber() +" не существует");
        }
        if (request.getAnswers().size() != questions.size()) {
            throw new IllegalArgumentException("Количество ответов не совпадает с количеством вопросов");
        }

        List<WrongAnswerDto> wrongAnswers = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            int correct = questions.get(i).getCorrectAnswerIndex();
            int user = request.getAnswers().get(i);
            if (correct != user) {
                QuestionEntity question = questions.get(i);
                WrongAnswerDto wrongAnswer = new WrongAnswerDto();

                wrongAnswer.setTicketNumber(request.getTicketNumber());
                wrongAnswer.setQuestionNumber(question.getQuestionNumber());
                wrongAnswer.setCorrectAnswerIndex(correct);
                wrongAnswer.setUserAnswerIndex(user);
                if (question.getQuestHelp() != null) {
                    wrongAnswer.setExplanation(question.getQuestHelp());
                }
                if (question.getTopic() != null) {
                    wrongAnswer.setTopicName(question.getTopic().getTopicName());
                }
                wrongAnswers.add(wrongAnswer);
            }
        }
        return wrongAnswers;
    }

    public List<RecommendationQuestionDto> getRecommendedQuestions(String telegramId, int count) {
        UserEntity user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<UserAttemptsEntity> attempts = userAttemptsRepository
                .findByUser_IdOrderByAttemptDateDesc(user.getId());

        if (attempts.isEmpty()) {
            return new ArrayList<>();
        }

        // Получаем топ-3 слабые темы (
        List<WeakTopicDto> weakTopics = statisticsService.getWeakTopics(attempts, 3);

        if (weakTopics.isEmpty()) {
            return new ArrayList<>();
        }

        // Получаем ID тем
        List<Long> topicIds = weakTopics.stream()
                .map(WeakTopicDto::getTopicId)
                .collect(Collectors.toList());

        // Получаем все вопросы по этим темам
        List<QuestionEntity> questions = questionRepository.findByTopic_IdIn(topicIds);

        // Перемешиваем и берём первые count вопросов
        Collections.shuffle(questions);
        List<QuestionEntity> selectedQuestions = questions.stream()
                .limit(count)
                .collect(Collectors.toList());

        // Преобразуем в DTO
        return selectedQuestions.stream()
                .map(this::convertToRecommendationDto)
                .collect(Collectors.toList());
    }

    private RecommendationQuestionDto convertToRecommendationDto(QuestionEntity entity) {
        RecommendationQuestionDto dto = new RecommendationQuestionDto();
        dto.setQuestionId(entity.getId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setAnswers(entity.getAnswersText());
        dto.setCorrectAnswerIndex(entity.getCorrectAnswerIndex());
        dto.setExplanation(entity.getQuestHelp());
        dto.setImageUrlSmall(entity.getImageUrlSmall());
        return dto;
    }

}
