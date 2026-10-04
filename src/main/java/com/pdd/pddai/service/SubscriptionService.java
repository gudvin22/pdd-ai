package com.pdd.pddai.service;

import com.pdd.pddai.dto.SubscriptionDto;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import com.pdd.pddai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final UserRepository userRepository;

    //  Проверка подписка
    public boolean isPremium(String telegramId) {
        UserEntity user = userRepository.findByTelegramId(telegramId).orElse(null);
        if (user == null) return false;

        return user.getSubscription() == SubscriptionTypeUser.PREMIUM
                && user.getSubscriptionEndDate() != null
                && user.getSubscriptionEndDate().isAfter(LocalDateTime.now());
    }

    // Активация / продление подписки
    public SubscriptionDto activateSubscription(String telegramId, int months) {
        UserEntity user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();

        // базовая дата: если подписка ещё активна — продлеваем от неё, иначе — от now
        LocalDateTime baseDate = (user.getSubscriptionEndDate() != null
                && user.getSubscriptionEndDate().isAfter(now))
                ? user.getSubscriptionEndDate()
                : now;

        LocalDateTime newEndDate = baseDate.plusMonths(months);

        user.setSubscription(SubscriptionTypeUser.PREMIUM);
        user.setSubscriptionEndDate(newEndDate);
        userRepository.save(user);

        return SubscriptionDto.builder()
                .subscription("PREMIUM")
                .subscriptionEndDate(newEndDate)
                .isActive(true)
                .message("Подписка активирована до " + newEndDate.format(DATE_FORMAT))
                .build();
    }

    // Информация о подписке
    public SubscriptionDto getSubscriptionInfo(String telegramId) {
        UserEntity user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        boolean isActive = user.getSubscription() == SubscriptionTypeUser.PREMIUM
                && user.getSubscriptionEndDate() != null
                && user.getSubscriptionEndDate().isAfter(LocalDateTime.now());

        String message;
        if (isActive) {
            message = "Подписка активна до " + user.getSubscriptionEndDate().format(DATE_FORMAT);
        } else if (user.getSubscriptionEndDate() != null) {
            message = "Подписка истекла " + user.getSubscriptionEndDate().format(DATE_FORMAT);
        } else {
            message = "Подписки нет";
        }

        return SubscriptionDto.builder()
                .subscription(user.getSubscription().name())
                .subscriptionEndDate(user.getSubscriptionEndDate())
                .isActive(isActive)
                .message(message)
                .build();
    }

    //  Отмена подписки (заглушка)
    public SubscriptionDto cancelSubscription(String telegramId) {
        // TODO: реализовать возврат через YooKassa
        return null;
    }
}