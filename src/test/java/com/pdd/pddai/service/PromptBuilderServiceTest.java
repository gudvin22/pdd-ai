package com.pdd.pddai.service;

import com.pdd.pddai.dto.AiAnalysisTicketRequestDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.TopicEntity;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptBuilderServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private PromptBuilderService promptBuilderService;

    // 1. Ошибки есть, вопросы найдены – промт содержит все детали
    @Test
    void buildPromptByErrorTicket_shouldIncludeAllErrorDetails() {
        // given
        AiAnalysisTicketRequestDto dto = new AiAnalysisTicketRequestDto();
        dto.setTicketNumber(8);

        // Ошибка 1
        AiAnalysisTicketRequestDto.ErrorDetail error1 = new AiAnalysisTicketRequestDto.ErrorDetail();
        error1.setQuestionNumber(1);
        error1.setUserAnswerIndex(0);
        error1.setCorrectAnswerIndex(2);

        // Ошибка 2
        AiAnalysisTicketRequestDto.ErrorDetail error2 = new AiAnalysisTicketRequestDto.ErrorDetail();
        error2.setQuestionNumber(3);
        error2.setUserAnswerIndex(1);
        error2.setCorrectAnswerIndex(3);

        dto.setErrors(List.of(error1, error2));

        // Вопросы из БД
        TopicEntity topic1 = new TopicEntity();
        topic1.setTopicName("Знаки");
        QuestionEntity q1 = new QuestionEntity();
        q1.setQuestionNumber(1);
        q1.setQuestionText("Какой знак?");
        q1.setAnswersText(List.of("А", "Б", "В"));
        q1.setTopic(topic1);

        TopicEntity topic3 = new TopicEntity();
        topic3.setTopicName("Скорость");
        QuestionEntity q3 = new QuestionEntity();
        q3.setQuestionNumber(3);
        q3.setQuestionText("Скорость?");
        q3.setAnswersText(List.of("40", "60", "80", "90"));
        q3.setTopic(topic3);

        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(8))
                .thenReturn(List.of(q1, q3));

        // when
        String prompt = promptBuilderService.buildPromptByErrorTicket(dto);

        // then – проверяем, что промт содержит нужные фрагменты
        // Исправлено: ищем "билет №8" вместо "Билет №8"
        assertThat(prompt).contains("билет №8")
                .contains("Ошибка в вопросе 1 (Тема: Знаки)")
                .contains("Вопрос: Какой знак?")
                .contains("Варианты ответов:")
                .contains("  1. А", "  2. Б", "  3. В")
                .contains("Пользователь выбрал: 1")
                .contains("Правильный ответ: 3")
                .contains("Ошибка в вопросе 3 (Тема: Скорость)")
                .contains("Вопрос: Скорость?")
                .contains("  1. 40", "  2. 60", "  3. 80", "  4. 90")
                .contains("Пользователь выбрал: 2")
                .contains("Правильный ответ: 4");

        verify(questionRepository, times(1)).findByTicketNumberOrderByQuestionNumberAsc(8);
    }

    // 2. Вопрос не найден – он пропускается, без исключений
    @Test
    void buildPromptByErrorTicket_whenQuestionNotFound_shouldSkipItAndContinue() {
        // given
        AiAnalysisTicketRequestDto dto = new AiAnalysisTicketRequestDto();
        dto.setTicketNumber(8);

        AiAnalysisTicketRequestDto.ErrorDetail error = new AiAnalysisTicketRequestDto.ErrorDetail();
        error.setQuestionNumber(99);
        error.setUserAnswerIndex(0);
        error.setCorrectAnswerIndex(1);
        dto.setErrors(List.of(error));

        // ВАЖНО: возвращаем не пустой список, а хотя бы один вопрос,
        // чтобы билет считался существующим.
        QuestionEntity q1 = new QuestionEntity();
        q1.setQuestionNumber(1);
        q1.setQuestionText("Какой знак?");
        q1.setAnswersText(List.of("А", "Б", "В"));

        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(8))
                .thenReturn(List.of(q1)); // не пустой список

        // when
        String prompt = promptBuilderService.buildPromptByErrorTicket(dto);

        // then
        assertThat(prompt).doesNotContain("Ошибка в вопросе 99");
        assertThat(prompt).contains("билет №8");
        // Метод не бросил исключение – тест проходит
    }

    // 3. Билет не найден – бросает TicketNotFoundException
    @Test
    void buildPromptByErrorTicket_whenTicketNotFound_shouldThrowException() {
        // given
        AiAnalysisTicketRequestDto dto = new AiAnalysisTicketRequestDto();
        dto.setTicketNumber(999);
        dto.setErrors(List.of());

        when(questionRepository.findByTicketNumberOrderByQuestionNumberAsc(999))
                .thenReturn(List.of());

        // then
        assertThatThrownBy(() -> promptBuilderService.buildPromptByErrorTicket(dto))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("Билета с номером 999 не существует");
    }
}