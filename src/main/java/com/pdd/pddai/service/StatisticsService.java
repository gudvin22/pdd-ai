package com.pdd.pddai.service;

import com.pdd.pddai.entity.UserAttemptsEntity;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.repository.UserAttemptsRepository;
import com.pdd.pddai.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final UserAttemptsRepository userAttemptsRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveAttempt(Long userId, Integer ticketNumber, Integer wrongCount, List<Integer> wrongTopicIds) {

        // Находим пользователя
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Создаём запись

        UserAttemptsEntity attempt= new UserAttemptsEntity();
        attempt.setUser(user);
        attempt.setTicketNumber(ticketNumber);
        attempt.setWrongCount(wrongCount);
        attempt.setWrongTopicIds(wrongTopicIds);

        userAttemptsRepository.save(attempt);



    }
}
