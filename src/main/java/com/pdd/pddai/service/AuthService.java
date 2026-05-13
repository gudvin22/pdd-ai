package com.pdd.pddai.service;

import com.pdd.pddai.dto.TelegramRegistrationDto;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.RoleUser;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import com.pdd.pddai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public String registerTelegramUser(TelegramRegistrationDto telegramRegistrationDto) throws Exception{

        if(userRepository.existsByTelegramId(telegramRegistrationDto.getTelegramId())) {
            throw new RuntimeException("Пользователь с таким TelegramId уже существует");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setTelegramId(telegramRegistrationDto.getTelegramId());
        userEntity.setUserName(telegramRegistrationDto.getUserName());
        userEntity.setRole(RoleUser.USER);
        userEntity.setSubscription(SubscriptionTypeUser.PREMIUM);
        userEntity.setSubscriptionEndDate(LocalDateTime.now().plusDays(3));

        userRepository.save(userEntity);

        return "ок";
    }

}
