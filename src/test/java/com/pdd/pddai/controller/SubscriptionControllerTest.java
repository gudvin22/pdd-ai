package com.pdd.pddai.controller;

import com.pdd.pddai.dto.SubscriptionDto;
import com.pdd.pddai.service.SubscriptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SubscriptionControllerTest {

    private SubscriptionController controller;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        // Создаем мок сервиса и вручную передаем его в контроллер
        subscriptionService = mock(SubscriptionService.class);
        controller = new SubscriptionController(subscriptionService);

        // Кладем telegramId в SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("12345", null)
        );
    }

    @AfterEach
    void tearDown() {
        // Очищаем контекст после каждого теста
        SecurityContextHolder.clearContext();
    }

    @Test
    void getInfo_shouldReturn200AndDto() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .subscription("PREMIUM")
                .isActive(true)
                .message("Подписка активна")
                .build();

        when(subscriptionService.getSubscriptionInfo("12345")).thenReturn(dto);

        ResponseEntity<SubscriptionDto> response = controller.getInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PREMIUM", response.getBody().getSubscription());
        assertEquals("Подписка активна", response.getBody().getMessage());
    }

    @Test
    void activate_shouldReturn200AndDto() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .subscription("PREMIUM")
                .isActive(true)
                .message("Подписка активирована")
                .build();

        when(subscriptionService.activateSubscription("12345", 1)).thenReturn(dto);

        ResponseEntity<SubscriptionDto> response = controller.activate();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PREMIUM", response.getBody().getSubscription());
        assertEquals("Подписка активирована", response.getBody().getMessage());
    }

    @Test
    void cancel_shouldReturn200() {
        when(subscriptionService.cancelSubscription("12345")).thenReturn(null);

        ResponseEntity<SubscriptionDto> response = controller.cancel();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getInfo_whenServiceThrows_shouldReturn500() {
        when(subscriptionService.getSubscriptionInfo(anyString()))
                .thenThrow(new RuntimeException("Тестовая ошибка"));

        ResponseEntity<SubscriptionDto> response = controller.getInfo();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}