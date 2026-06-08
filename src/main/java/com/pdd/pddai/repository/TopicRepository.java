package com.pdd.pddai.repository;

import com.pdd.pddai.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<TopicEntity, Long> {
    Optional<TopicEntity> findByTopicName(String topicName);

}
