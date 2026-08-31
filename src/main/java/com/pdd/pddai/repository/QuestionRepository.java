package com.pdd.pddai.repository;

import com.pdd.pddai.entity.QuestionEntity;
import org.springframework.boot.jackson.JacksonMixinModuleEntries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    boolean existsByCategoryAndTicketNumberAndQuestionNumber(String category, Integer ticketNumber, Integer questionNumber);

    List<QuestionEntity> findByTicketNumber(int ticketNumber);

    List<QuestionEntity> findByQuestHelpIsNull();

    List<QuestionEntity> findByTicketNumberOrderByQuestionNumberAsc(int ticketNumber);

    @Query("SELECT t.topicName FROM TopicEntity t WHERE t.id = :topicId")
    Optional<String> findTopicNameById(@Param("topicId") Long topicId);

    List<QuestionEntity> findByTopic_IdIn(List<Long> topicIds);
}
