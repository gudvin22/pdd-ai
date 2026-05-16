package com.pdd.pddai.service;

import com.pdd.pddai.config.JwtProperties;
import com.pdd.pddai.dto.TelegramRegistrationDto;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.RoleUser;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import com.pdd.pddai.exception.UserAlreadyExistsException;
import com.pdd.pddai.repository.UserRepository;
import com.pdd.pddai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public String registerTelegramUser(TelegramRegistrationDto telegramRegistrationDto) throws Exception{

//        if(userRepository.existsByTelegramId(telegramRegistrationDto.getTelegramId())) {
//            throw new UserAlreadyExistsException("Пользователь уже существует");
//        }

        UserEntity user = userRepository.findByTelegramId(telegramRegistrationDto.getTelegramId())
                .orElseGet(() -> {
                    // Если не найден — создаём нового
                    UserEntity newUser = new UserEntity();
                    newUser.setTelegramId(telegramRegistrationDto.getTelegramId());
                    newUser.setUserName(telegramRegistrationDto.getUserName());
                    newUser.setRole(RoleUser.USER);
                    newUser.setSubscription(SubscriptionTypeUser.PREMIUM);
                    newUser.setSubscriptionEndDate(LocalDateTime.now().plusDays(3));

                    return userRepository.save(newUser);
                });



        return jwtUtil.generateToken(user.getTelegramId());
    }

}
