package com.pdd.pddai.repository;

import com.pdd.pddai.entity.UserAttemptsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAttemptsRepository extends JpaRepository<UserAttemptsEntity, Long> {
    // Все попытки пользователя, сортировка по дате (сначала новые)
    List<UserAttemptsEntity> findByUser_IdOrderByAttemptDateDesc(Long userId);

    // Последняя попытка по конкретному билету
    Optional<UserAttemptsEntity> findFirstByUser_IdAndTicketNumberOrderByAttemptDateDesc(Long userId, Integer ticketNumber);

    // Существует ли хотя бы одна попытка по билету
    boolean existsByUser_IdAndTicketNumber(Long userId, Integer ticketNumber);}
