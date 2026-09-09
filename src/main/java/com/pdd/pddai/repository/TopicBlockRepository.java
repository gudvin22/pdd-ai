package com.pdd.pddai.repository;

import com.pdd.pddai.entity.TopicBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicBlockRepository extends JpaRepository<TopicBlockEntity, Long> {

    List<TopicBlockEntity> findByBlockNumber(Integer blockNumber);

}
