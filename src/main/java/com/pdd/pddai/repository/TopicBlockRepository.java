package com.pdd.pddai.repository;

import com.pdd.pddai.entity.TopicBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicBlockRepository extends JpaRepository<TopicBlockEntity, Long> {

    List<TopicBlockEntity> findByBlockNumber(Integer blockNumber);

    Optional<TopicBlockEntity> findByTopicId(Long topicId);

}
