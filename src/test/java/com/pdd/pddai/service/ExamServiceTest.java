package com.pdd.pddai.service;

import com.pdd.pddai.dto.ExamCheckRequestDto;
import com.pdd.pddai.dto.QuestionResponseDto;
import com.pdd.pddai.dto.WrongAnswerDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.TopicEntity;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private ExamService examService;

    @Test
    void getTicketNumber_whenTicketExists_shouldReturnQuestionsInOrder() {
        // 1. Подготовка
        int ticketNumber = 8;

        QuestionEntity q1 = new QuestionEntity();
        q1.setQuestionNumber(1);
        q1.setQuestionText("Вопрос 1");
        q1.setAnswersText(List.of("A", "B", "C"));
        q1.setImageUrlSmall("img1.jpg");

        QuestionEntity q2 = new QuestionEntity();
        q2.setQuestionNumber(2);
        q2.setQuestionText("Вопрос 2");
        q2.setAnswersText(List.of("D", "E", "F"));
        q2.setImageUrlSmall("img2.jpg");

        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of(q1, q2));

        // 2. Действие
        List<QuestionResponseDto> result = examService.getTicketNumber(ticketNumber);

        // 3. Проверка
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuestionNumber()).isEqualTo(1);
        assertThat(result.get(0).getQuestionText()).isEqualTo("Вопрос 1");
        assertThat(result.get(1).getQuestionNumber()).isEqualTo(2);
        assertThat(result.get(1).getQuestionText()).isEqualTo("Вопрос 2");

        verify(questionRepository, times(1))
                .findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);
    }

    @Test
    void getTicketNumber_whenTicketNotFound_shouldThrowException() {
        // 1. Подготовка
        int ticketNumber = 999;
        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of());

        // 2. Действие + проверка
        assertThatThrownBy(() -> examService.getTicketNumber(ticketNumber))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("Билета с номером " + ticketNumber + " не существует");
    }

    @Test
    void checkExam_whenAllAnswersCorrect_shouldReturnEmptyList() {
        // 1. Подготовка
        int ticketNumber = 8;
        List<Integer> userAnswers = List.of(0, 1, 2); // индексы ответов пользователя (все правильные)

        ExamCheckRequestDto request = new ExamCheckRequestDto();
        request.setTicketNumber(ticketNumber);
        request.setAnswers(userAnswers);

        // Вопросы из БД
        QuestionEntity q1 = new QuestionEntity();
        q1.setQuestionNumber(1);
        q1.setCorrectAnswerIndex(0);

        QuestionEntity q2 = new QuestionEntity();
        q2.setQuestionNumber(2);
        q2.setCorrectAnswerIndex(1);

        QuestionEntity q3 = new QuestionEntity();
        q3.setQuestionNumber(3);
        q3.setCorrectAnswerIndex(2);

        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of(q1, q2, q3));

        // 2. Действие
        List<WrongAnswerDto> result = examService.checkExam(request);

        // 3. Проверка
        assertThat(result).isEmpty();
        verify(questionRepository, times(1))
                .findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);
    }

    @Test
    void checkExam_whenSomeAnswersWrong_shouldReturnListOfWrongAnswersWithDetails() {
        // 1. Подготовка
        int ticketNumber = 8;
        // Ответы пользователя: на первый вопрос ошибся (выбрал 1 вместо 0),
        // на второй правильно, на третий ошибся (выбрал 1 вместо 2).
        List<Integer> userAnswers = List.of(1, 1, 1);

        ExamCheckRequestDto request = new ExamCheckRequestDto();
        request.setTicketNumber(ticketNumber);
        request.setAnswers(userAnswers);

        // Вопросы из БД
        TopicEntity topic1 = new TopicEntity();
        topic1.setTopicName("Тема 1");

        QuestionEntity q1 = new QuestionEntity();
        q1.setQuestionNumber(1);
        q1.setCorrectAnswerIndex(0);
        q1.setQuestHelp("Объяснение 1");
        q1.setTopic(topic1);

        TopicEntity topic2 = new TopicEntity();
        topic2.setTopicName("Тема 2");

        QuestionEntity q2 = new QuestionEntity();
        q2.setQuestionNumber(2);
        q2.setCorrectAnswerIndex(1);
        q2.setQuestHelp("Объяснение 2");
        q2.setTopic(topic2);

        QuestionEntity q3 = new QuestionEntity();
        q3.setQuestionNumber(3);
        q3.setCorrectAnswerIndex(2);
        q3.setQuestHelp("Объяснение 3");
        q3.setTopic(topic1); // можно привязать тему

        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of(q1, q2, q3));

        // 2. Действие
        List<WrongAnswerDto> result = examService.checkExam(request);

        // 3. Проверка
        assertThat(result).hasSize(2); // две ошибки

        // Первая ошибка (вопрос 1)
        WrongAnswerDto wrong1 = result.get(0);
        assertThat(wrong1.getQuestionNumber()).isEqualTo(1);
        assertThat(wrong1.getUserAnswerIndex()).isEqualTo(1);
        assertThat(wrong1.getCorrectAnswerIndex()).isEqualTo(0);
        assertThat(wrong1.getExplanation()).isEqualTo("Объяснение 1");
        assertThat(wrong1.getTopicName()).isEqualTo("Тема 1");

        // Вторая ошибка (вопрос 3)
        WrongAnswerDto wrong2 = result.get(1);
        assertThat(wrong2.getQuestionNumber()).isEqualTo(3);
        assertThat(wrong2.getUserAnswerIndex()).isEqualTo(1);
        assertThat(wrong2.getCorrectAnswerIndex()).isEqualTo(2);
        assertThat(wrong2.getExplanation()).isEqualTo("Объяснение 3");
        assertThat(wrong2.getTopicName()).isEqualTo("Тема 1");

        verify(questionRepository, times(1))
                .findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);
    }

    @Test
    void checkExam_whenAnswersCountMismatch_shouldThrowException() {
        // 1. Подготовка
        int ticketNumber = 8;
        // Ответов меньше, чем вопросов (например, 2 вместо 3)
        List<Integer> userAnswers = List.of(0, 1);

        ExamCheckRequestDto request = new ExamCheckRequestDto();
        request.setTicketNumber(ticketNumber);
        request.setAnswers(userAnswers);

        // Репозиторий возвращает 3 вопроса
        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of(new QuestionEntity(), new QuestionEntity(), new QuestionEntity()));

        // 2. Действие + проверка
        assertThatThrownBy(() -> examService.checkExam(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Количество ответов не совпадает с количеством вопросов");
    }

    @Test
    void checkExam_whenTicketNotFound_shouldThrowException() {
        // 1. Подготовка
        int ticketNumber = 999;
        ExamCheckRequestDto request = new ExamCheckRequestDto();
        request.setTicketNumber(ticketNumber);
        request.setAnswers(List.of(0, 1));

        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of());

        // 2. Действие + проверка
        assertThatThrownBy(() -> examService.checkExam(request))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("Билета с номером " + ticketNumber + " не существует");
    }
}