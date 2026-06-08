package com.pdd.pddai.service;

import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import com.pdd.pddai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void setLastActiveDate(String telegramId) {
        UserEntity user = userRepository.findByTelegramId(telegramId).orElse(null);
        if (user != null) {
            user.setLastActiveDate(LocalDateTime.now());
            checkAndUpdateSubscription(user);
            userRepository.save(user);
        }
    }

    private void checkAndUpdateSubscription(UserEntity user) {
        if (user.getSubscriptionEndDate() != null &&
                user.getSubscriptionEndDate().isBefore(LocalDateTime.now())) {
            user.setSubscription(SubscriptionTypeUser.FREE);
        }
    }
}
