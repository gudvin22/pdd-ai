package com.pdd.pddai.repository;

import com.pdd.pddai.entity.QuestionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void findByTicketNumberOrderByQuestionNumberAsc_shouldReturnSortedQuestions() {
        // Проверяем на реальном билете (например, 1)
        List<QuestionEntity> questions = questionRepository.findByTicketNumberOrderByQuestionNumberAsc(1);
        assertThat(questions).isNotEmpty();
        for (int i = 0; i < questions.size() - 1; i++) {
            assertThat(questions.get(i).getQuestionNumber())
                    .isLessThan(questions.get(i + 1).getQuestionNumber());
        }
    }
}