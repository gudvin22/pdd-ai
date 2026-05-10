package com.pdd.pddai.repository;

import com.pdd.pddai.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByTelegramId(String telegramId);
    boolean existsByTelegramId(String telegramId);
}
