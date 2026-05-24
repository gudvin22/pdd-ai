package com.pdd.pddai.repository;

import com.pdd.pddai.entity.QuestionEntity;
import org.springframework.boot.jackson.JacksonMixinModuleEntries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    boolean existsByCategoryAndTicketNumberAndQuestionNumber(String category, Integer ticketNumber, Integer questionNumber);

    List<QuestionEntity> findByTicketNumber(int ticketNumber);
}
