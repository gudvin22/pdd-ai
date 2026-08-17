package com.pdd.pddai.service;

import com.pdd.pddai.dto.WrongAnswerDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.TopicEntity;
import com.pdd.pddai.entity.UserAttemptsEntity;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.repository.QuestionRepository;
import com.pdd.pddai.repository.UserAttemptsRepository;
import com.pdd.pddai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private UserAttemptsRepository userAttemptsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void collectAndSaveAttempt_shouldSaveAttempt_whenUserExistsAndQuestionsFound() {
        // given
        String telegramId = "12345";
        int ticketNumber = 8;
        List<WrongAnswerDto> wrongAnswers = List.of(
                createWrongAnswerDto(1, 1, 0),
                createWrongAnswerDto(3, 0, 2)
        );

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setTelegramId(telegramId);

        // Вопросы билета
        TopicEntity topic1 = new TopicEntity();
        topic1.setId(5L);
        TopicEntity topic2 = new TopicEntity();
        topic2.setId(7L);

        QuestionEntity q1 = new QuestionEntity();
        q1.setQuestionNumber(1);
        q1.setTopic(topic1);

        QuestionEntity q3 = new QuestionEntity();
        q3.setQuestionNumber(3);
        q3.setTopic(topic2);

        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));
        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of(q1, q3));

        // when
        statisticsService.collectAndSaveAttempt(telegramId, ticketNumber, wrongAnswers);

        // then
        ArgumentCaptor<UserAttemptsEntity> captor = ArgumentCaptor.forClass(UserAttemptsEntity.class);
        verify(userAttemptsRepository, times(1)).save(captor.capture());
        UserAttemptsEntity saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTicketNumber()).isEqualTo(ticketNumber);
        assertThat(saved.getWrongCount()).isEqualTo(2);
        assertThat(saved.getWrongTopicIds()).containsExactlyInAnyOrder(5, 7);

        verify(userRepository, times(1)).findByTelegramId(telegramId);
        verify(questionRepository, times(1)).findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);
    }

    @Test
    void collectAndSaveAttempt_shouldThrowException_whenUserNotFound() {
        // given
        String telegramId = "unknown";
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> statisticsService.collectAndSaveAttempt(telegramId, 8, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");

        verify(userAttemptsRepository, never()).save(any());
    }

    @Test
    void collectAndSaveAttempt_shouldHandleMissingQuestionsGracefully() {
        // given
        String telegramId = "12345";
        int ticketNumber = 999; // билета нет
        List<WrongAnswerDto> wrongAnswers = List.of(
                createWrongAnswerDto(1, 0, 1)
        );

        UserEntity user = new UserEntity();
        user.setId(1L);

        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));
        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber))
                .thenReturn(List.of()); // пустой список – билет не найден

        // when
        statisticsService.collectAndSaveAttempt(telegramId, ticketNumber, wrongAnswers);

        // then
        ArgumentCaptor<UserAttemptsEntity> captor = ArgumentCaptor.forClass(UserAttemptsEntity.class);
        verify(userAttemptsRepository, times(1)).save(captor.capture());
        UserAttemptsEntity saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTicketNumber()).isEqualTo(ticketNumber);
        assertThat(saved.getWrongCount()).isEqualTo(1);
        assertThat(saved.getWrongTopicIds()).isEmpty(); // т.к. вопрос не найден

        verify(questionRepository, times(1)).findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);
    }

    // Вспомогательный метод для создания DTO ошибки
    private WrongAnswerDto createWrongAnswerDto(int questionNumber, int userAnswer, int correctAnswer) {
        WrongAnswerDto dto = new WrongAnswerDto();
        dto.setQuestionNumber(questionNumber);
        dto.setUserAnswerIndex(userAnswer);
        dto.setCorrectAnswerIndex(correctAnswer);
        return dto;
    }
}