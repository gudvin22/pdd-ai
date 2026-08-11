package com.pdd.pddai.service;


import com.pdd.pddai.repository.UserRepository;
import com.pdd.pddai.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import com.pdd.pddai.dto.TelegramRegistrationDto;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.enums.RoleUser;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerTelegramUser_whenUserExists_shouldReturnTokenAndNotCreateNew() throws Exception {
        // 1. Подготовка (Arrange)
        TelegramRegistrationDto dto = new TelegramRegistrationDto();
        dto.setTelegramId("123");
        dto.setUserName("testUser");

        UserEntity existingUser = new UserEntity();
        existingUser.setTelegramId("123");
        existingUser.setUserName("testUser");

        // Настраиваем моки
        when(userRepository.findByTelegramId("123")).thenReturn(Optional.of(existingUser));
        when(jwtUtil.generateToken("123")).thenReturn("fake.jwt.token");

        // 2. Вызов тестируемого метода (Act)
        String result = authService.registerTelegramUser(dto);

        // 3. Проверка (Assert)
        assertThat(result).isEqualTo("fake.jwt.token");

        // Убеждаемся, что метод save НЕ вызывался
        verify(userRepository, never()).save(any(UserEntity.class));
        // Убеждаемся, что generateToken вызывался ровно 1 раз
        verify(jwtUtil, times(1)).generateToken("123");
    }

    @Test
    void registerTelegramUser_whenUserDoesNotExist_shouldCreateNewUserAndReturnToken() throws Exception {
        // 1. Подготовка (Arrange)
        TelegramRegistrationDto dto = new TelegramRegistrationDto();
        dto.setTelegramId("123");
        dto.setUserName("newUser");

        // Мокаем, что пользователь не найден
        when(userRepository.findByTelegramId("123")).thenReturn(Optional.empty());

        // Создаём объект, который "вернёт" save()
        UserEntity savedUser = new UserEntity();
        savedUser.setTelegramId("123");
        savedUser.setUserName("newUser");
        savedUser.setRole(RoleUser.USER);
        savedUser.setSubscription(SubscriptionTypeUser.PREMIUM);
        savedUser.setSubscriptionEndDate(LocalDateTime.now().plusDays(3));

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("123")).thenReturn("fake.jwt.token");

        // 2. Действие
        String result = authService.registerTelegramUser(dto);

        // 3. Проверка
        assertThat(result).isEqualTo("fake.jwt.token");

        // Проверяем, что save вызывался ровно 1 раз, и захватываем переданный объект
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(captor.capture());
        UserEntity captured = captor.getValue();

        // Проверяем поля сохранённого пользователя
        assertThat(captured.getTelegramId()).isEqualTo("123");
        assertThat(captured.getUserName()).isEqualTo("newUser");
        assertThat(captured.getRole()).isEqualTo(RoleUser.USER);
        assertThat(captured.getSubscription()).isEqualTo(SubscriptionTypeUser.PREMIUM);
        // Проверяем дату с погрешностью
        assertThat(captured.getSubscriptionEndDate())
                .isAfter(LocalDateTime.now().plusDays(2).minusMinutes(1))
                .isBefore(LocalDateTime.now().plusDays(4).plusMinutes(1));

        verify(jwtUtil, times(1)).generateToken("123");
    }



}