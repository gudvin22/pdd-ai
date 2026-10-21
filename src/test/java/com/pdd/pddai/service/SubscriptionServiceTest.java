package com.pdd.pddai.service;

import com.pdd.pddai.dto.SubscriptionDto;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import com.pdd.pddai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    // ========== isPremium ==========

    @Test
    void isPremium_UserNotFound_ReturnsFalse() {
        when(userRepository.findByTelegramId("999")).thenReturn(Optional.empty());
        assertFalse(subscriptionService.isPremium("999"));
    }

    @Test
    void isPremium_FreeUser_ReturnsFalse() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.FREE);
        user.setSubscriptionEndDate(LocalDateTime.now().plusDays(10));

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));

        assertFalse(subscriptionService.isPremium("123"));
    }

    @Test
    void isPremium_PremiumButExpired_ReturnsFalse() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));

        assertFalse(subscriptionService.isPremium("123"));
    }

    @Test
    void isPremium_PremiumAndNotExpired_ReturnsTrue() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(LocalDateTime.now().plusDays(10));

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));

        assertTrue(subscriptionService.isPremium("123"));
    }

    // ========== activateSubscription ==========

    @Test
    void activateSubscription_NewUser_ExtendsFromNow() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.FREE);
        user.setSubscriptionEndDate(null);

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionDto result = subscriptionService.activateSubscription("123", 1);

        assertEquals("PREMIUM", result.getSubscription());
        assertTrue(result.isActive());
        assertNotNull(result.getSubscriptionEndDate());

        // Дата должна быть примерно now + 1 месяц
        LocalDateTime expected = LocalDateTime.now().plusMonths(1);
        assertTrue(result.getSubscriptionEndDate().isAfter(expected.minusMinutes(1)));
        assertTrue(result.getSubscriptionEndDate().isBefore(expected.plusMinutes(1)));

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void activateSubscription_ActiveSubscription_ExtendsFromEndDate() {
        LocalDateTime currentEnd = LocalDateTime.now().plusDays(10);

        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(currentEnd);

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionDto result = subscriptionService.activateSubscription("123", 1);

        // Дата должна быть currentEnd + 1 месяц
        LocalDateTime expected = currentEnd.plusMonths(1);
        assertEquals(expected, result.getSubscriptionEndDate());
        assertEquals("PREMIUM", result.getSubscription());
        assertTrue(result.isActive());
    }

    @Test
    void activateSubscription_ExpiredSubscription_ExtendsFromNow() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.FREE);
        user.setSubscriptionEndDate(LocalDateTime.now().minusDays(5)); // истекла

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionDto result = subscriptionService.activateSubscription("123", 1);

        // Дата должна быть от now (не от старой endDate)
        LocalDateTime expected = LocalDateTime.now().plusMonths(1);
        assertTrue(result.getSubscriptionEndDate().isAfter(expected.minusMinutes(1)));
        assertTrue(result.getSubscriptionEndDate().isBefore(expected.plusMinutes(1)));
    }

    @Test
    void activateSubscription_UserNotFound_ThrowsException() {
        when(userRepository.findByTelegramId("999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> subscriptionService.activateSubscription("999", 1));
    }

    // ========== getSubscriptionInfo ==========

    @Test
    void getSubscriptionInfo_ActiveSubscription_ReturnsActiveTrue() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(LocalDateTime.now().plusDays(10));

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));

        SubscriptionDto result = subscriptionService.getSubscriptionInfo("123");

        assertEquals("PREMIUM", result.getSubscription());
        assertTrue(result.isActive());
        assertTrue(result.getMessage().startsWith("Подписка активна до"));
    }

    @Test
    void getSubscriptionInfo_ExpiredSubscription_ReturnsActiveFalse() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(LocalDateTime.now().minusDays(5));

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));

        SubscriptionDto result = subscriptionService.getSubscriptionInfo("123");

        assertFalse(result.isActive());
        assertTrue(result.getMessage().startsWith("Подписка истекла"));
    }

    @Test
    void getSubscriptionInfo_NoSubscription_ReturnsNoSubscriptionMessage() {
        UserEntity user = new UserEntity();
        user.setSubscription(SubscriptionTypeUser.FREE);
        user.setSubscriptionEndDate(null);

        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(user));

        SubscriptionDto result = subscriptionService.getSubscriptionInfo("123");

        assertFalse(result.isActive());
        assertEquals("Подписки нет", result.getMessage());
    }
}