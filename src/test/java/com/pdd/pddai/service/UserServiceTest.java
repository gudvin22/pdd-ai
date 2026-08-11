package com.pdd.pddai.service;

import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import com.pdd.pddai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final String TELEGRAM_ID = "12345";

    // 1. Пользователь найден, подписка активна
    @Test
    void setLastActiveDate_whenUserExistsAndSubscriptionActive_shouldUpdateDateAndKeepSubscription() {
        // given
        UserEntity user = new UserEntity();
        user.setTelegramId(TELEGRAM_ID);
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(LocalDateTime.now().plusDays(1));

        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(user));

        // when
        userService.setLastActiveDate(TELEGRAM_ID);

        // then
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(captor.capture());

        UserEntity saved = captor.getValue();
        assertThat(saved.getLastActiveDate()).isAfter(LocalDateTime.now().minusSeconds(2));
        assertThat(saved.getSubscription()).isEqualTo(SubscriptionTypeUser.PREMIUM);
    }

    // 2. Пользователь найден, подписка истекла
    @Test
    void setLastActiveDate_whenUserExistsAndSubscriptionExpired_shouldUpdateDateAndSetSubscriptionToFree() {
        // given
        UserEntity user = new UserEntity();
        user.setTelegramId(TELEGRAM_ID);
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(user));

        // when
        userService.setLastActiveDate(TELEGRAM_ID);

        // then
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(captor.capture());

        UserEntity saved = captor.getValue();
        assertThat(saved.getLastActiveDate()).isAfter(LocalDateTime.now().minusSeconds(2));
        assertThat(saved.getSubscription()).isEqualTo(SubscriptionTypeUser.FREE);
    }

    // 3. Пользователь найден, но subscriptionEndDate == null
    @Test
    void setLastActiveDate_whenUserExistsAndSubscriptionEndDateIsNull_shouldUpdateDateAndKeepSubscription() {
        // given
        UserEntity user = new UserEntity();
        user.setTelegramId(TELEGRAM_ID);
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(null); // нет даты окончания

        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(user));

        // when
        userService.setLastActiveDate(TELEGRAM_ID);

        // then
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(captor.capture());

        UserEntity saved = captor.getValue();
        assertThat(saved.getLastActiveDate()).isAfter(LocalDateTime.now().minusSeconds(2));
        assertThat(saved.getSubscription()).isEqualTo(SubscriptionTypeUser.PREMIUM);
    }

    // 4. Пользователь не найден – save не вызывается
    @Test
    void setLastActiveDate_whenUserNotFound_shouldNotCallSave() {
        // given
        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.empty());

        // when
        userService.setLastActiveDate(TELEGRAM_ID);

        // then
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}