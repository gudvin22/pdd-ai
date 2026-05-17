package com.pdd.pddai.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final com.pdd.pddai.util.JwtUtil jwtUtil; // полный путь, чтобы IDEA не ругалась

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // обрезаем "Bearer "

            // Проверяем токен
            if (jwtUtil.validateToken(token)) {
                // извлекаем telegramId
                String telegramId = jwtUtil.getTelegramIdFromToken(token);

                //Создаём объект аутентификации для Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(telegramId, null, null);

                // Кладём его в контекст безопасности
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        //пропускаем запрос дальше
        filterChain.doFilter(request, response);
    }
}