package com.pdd.pddai.service;

import com.pdd.pddai.dto.*;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.TopicBlockEntity;
import com.pdd.pddai.entity.UserAttemptsEntity;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.*;
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
    private final TopicBlockRepository  topicBlockRepository;



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
        dto.setCorrectAnswerIndex(entity.getCorrectAnswerIndex());
        return dto;
    }

    public ExamResultDto checkExamStrict(ExamCheckRequestDto request) {

        List<WrongAnswerDto> wrongAnswers = checkExam(request);
        int totalErrors = wrongAnswers.size();

        // ошибки по блокам (Map: блок -> количество ошибок)
        Map<Integer, Integer> errorCountByBlock = new HashMap<>();
        for (WrongAnswerDto wrong : wrongAnswers) {
            Integer block = getBlockByQuestionNumber(wrong.getQuestionNumber(), request.getTicketNumber());
            if (block != null) {
                errorCountByBlock.put(block, errorCountByBlock.getOrDefault(block, 0) + 1);
            }
        }

        boolean passed = false;
        String message = "";
        List<QuestionResponseDto> additionalQuestions = new ArrayList<>();


        if (totalErrors == 0) {
            // 0 ошибок → сдан
            passed = true;
            message = "✅ Экзамен сдан! Отлично!";
        } else if (totalErrors >= 3) {
            // 3+ ошибок → не сдан
            passed = false;
            message = "❌ Экзамен не сдан. 3 и более ошибок.";
        } else if (totalErrors == 1) {
            // 1 ошибка → +5 вопросов из блока ошибки
            Integer block = errorCountByBlock.keySet().iterator().next();
            additionalQuestions = getAdditionalQuestions(block, request.getTicketNumber(), 5);
            passed = false;
            message = "⚠️ 1 ошибка. Ответьте на 5 дополнительных вопросов из блока " + block + ".";
        } else if (totalErrors == 2) {
            // Проверяем, в одном ли блоке ошибки
            if (errorCountByBlock.size() == 1) {
                // 2 ошибки в одном блоке → не сдан
                passed = false;
                message = "❌ Экзамен не сдан. 2 ошибки в одном блоке.";
            } else {
                // 2 ошибки в разных блоках → +10 вопросов
                List<Integer> blocks = new ArrayList<>(errorCountByBlock.keySet());
                additionalQuestions.addAll(getAdditionalQuestions(blocks.get(0), request.getTicketNumber(), 5));
                additionalQuestions.addAll(getAdditionalQuestions(blocks.get(1), request.getTicketNumber(), 5));
                passed = false;
                message = "⚠️ 2 ошибки в разных блоках. Ответьте на 10 дополнительных вопросов.";
            }
        }

        ExamResultDto result = new ExamResultDto();
        result.setPassed(passed);
        result.setMessage(message);
        result.setWrongAnswers(wrongAnswers);
        result.setAdditionalQuestions(additionalQuestions);
        return result;
    }

    private List<QuestionResponseDto> getAdditionalQuestions(Integer block, int ticketNumber, int count) {
        // Получаем вопросы из блока, исключая вопросы текущего билета
        List<QuestionEntity> questions = questionRepository.findByBlockAndNotInTicket(block, ticketNumber);
        Collections.shuffle(questions);

        // Берём нужное количество
        return questions.stream()
                .limit(count)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private Integer getBlockByQuestionNumber(int questionNumber, int ticketNumber) {
        //Находим вопрос по номеру билета и номеру вопроса
        QuestionEntity question = questionRepository
                .findByTicketNumberAndQuestionNumber(ticketNumber, questionNumber)
                .orElse(null);

        if (question == null || question.getTopic() == null) {
            return null;
        }

        //Определяем блок по теме вопроса
        return topicBlockRepository.findByTopicId(question.getTopic().getId())
                .map(TopicBlockEntity::getBlockNumber)
                .orElse(null);
    }

    //--------------------------------------------------------------------------


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
