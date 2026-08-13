package com.pdd.pddai.service;

import com.pdd.pddai.entity.UserAttemptsEntity;
import com.pdd.pddai.entity.UserEntity;
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

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void saveAttempt_shouldSaveAttempt_whenUserExists() {
        // given
        Long userId = 1L;
        Integer ticketNumber = 8;
        Integer wrongCount = 3;
        List<Integer> wrongTopicIds = List.of(5, 12, 7);

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTelegramId("12345");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        statisticsService.saveAttempt(userId, ticketNumber, wrongCount, wrongTopicIds);

        // then
        ArgumentCaptor<UserAttemptsEntity> captor = ArgumentCaptor.forClass(UserAttemptsEntity.class);
        verify(userAttemptsRepository, times(1)).save(captor.capture());

        UserAttemptsEntity saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTicketNumber()).isEqualTo(ticketNumber);
        assertThat(saved.getWrongCount()).isEqualTo(wrongCount);
        assertThat(saved.getWrongTopicIds()).isEqualTo(wrongTopicIds);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void saveAttempt_shouldThrowException_whenUserNotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> statisticsService.saveAttempt(userId, 8, 0, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");

        verify(userAttemptsRepository, never()).save(any());
    }
}