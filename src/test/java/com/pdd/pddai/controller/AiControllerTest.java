package com.pdd.pddai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdd.pddai.dto.AiAnalysisTicketRequestDto;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.service.AgentService;
import com.pdd.pddai.service.PromptBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private AgentService agentService;

    @InjectMocks
    private AiController aiController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    }

    @Test
    void analyzeError_shouldReturn200_whenValidRequest() throws Exception {
        // given
        AiAnalysisTicketRequestDto request = new AiAnalysisTicketRequestDto();
        request.setTicketNumber(8);
        AiAnalysisTicketRequestDto.ErrorDetail error = new AiAnalysisTicketRequestDto.ErrorDetail();
        error.setQuestionNumber(3);
        error.setUserAnswerIndex(1);
        error.setCorrectAnswerIndex(3);
        request.setErrors(List.of(error));

        when(promptBuilderService.buildPromptByErrorTicket(any())).thenReturn("prompt");
        when(agentService.askAgent(any())).thenReturn("some response");

        // when + then
        mockMvc.perform(post("/api/ai/analyze-errors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void analyzeError_shouldReturn404_whenTicketNotFound() throws Exception {
        // given
        AiAnalysisTicketRequestDto request = new AiAnalysisTicketRequestDto();
        request.setTicketNumber(999);
        request.setErrors(List.of());

        when(promptBuilderService.buildPromptByErrorTicket(any()))
                .thenThrow(new TicketNotFoundException("Билета с номером 999 не существует"));

        // when + then
        mockMvc.perform(post("/api/ai/analyze-errors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void analyzeError_shouldReturn500_whenAgentFails() throws Exception {
        // given
        AiAnalysisTicketRequestDto request = new AiAnalysisTicketRequestDto();
        request.setTicketNumber(8);
        AiAnalysisTicketRequestDto.ErrorDetail error = new AiAnalysisTicketRequestDto.ErrorDetail();
        error.setQuestionNumber(1);
        error.setUserAnswerIndex(0);
        error.setCorrectAnswerIndex(1);
        request.setErrors(List.of(error));

        when(promptBuilderService.buildPromptByErrorTicket(any())).thenReturn("prompt");
        when(agentService.askAgent(any())).thenThrow(new RuntimeException("Ошибка агента"));

        // when + then
        mockMvc.perform(post("/api/ai/analyze-errors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}