package com.pdd.pddai.service;

import com.pdd.pddai.dto.ExamCheckRequestDto;
import com.pdd.pddai.dto.QuestionResponseDto;
import com.pdd.pddai.dto.WrongAnswerDto;
import com.pdd.pddai.entity.QuestionEntity;
import com.pdd.pddai.entity.UserEntity;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final QuestionRepository questionRepository;
    private final StatisticsService statisticsService;

            public List<QuestionResponseDto> getTicketNumber (int ticketNumber) {

                List<QuestionEntity> questions = questionRepository.findByTicketNumberOrderByQuestionNumberAsc(ticketNumber);
                if (questions.isEmpty()) {
                    throw new TicketNotFoundException("Билета с номером " +ticketNumber +" не существует");
                }

                return questions.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            }


    private QuestionResponseDto convertToDto(QuestionEntity entity) {
        QuestionResponseDto dto = new QuestionResponseDto();
        dto.setQuestionNumber(entity.getQuestionNumber());
        dto.setQuestionText(entity.getQuestionText());
        dto.setAnswersText(entity.getAnswersText());
        dto.setImageUrlSmall(entity.getImageUrlSmall());
        return dto;
    }


    public List<WrongAnswerDto> checkExam(ExamCheckRequestDto request) {

        List<QuestionEntity> questions = questionRepository.findByTicketNumberOrderByQuestionNumberAsc(request.getTicketNumber());
        if (questions.isEmpty()) {
            throw new TicketNotFoundException("Билета с номером " +request.getTicketNumber() +" не существует");
        }
        if (request.getAnswers().size() != questions.size()) {
            throw new IllegalArgumentException("Количество ответов не совпадает с количеством вопросов");
        }

        List<WrongAnswerDto> wrongAnswers = new ArrayList<>();


        for (int i = 0; i < questions.size(); i++) {
            int correct = questions.get(i).getCorrectAnswerIndex();
            int user = request.getAnswers().get(i);
            if (correct != user) {
                QuestionEntity question = questions.get(i);
                WrongAnswerDto wrongAnswer = new WrongAnswerDto();

                wrongAnswer.setTicketNumber(request.getTicketNumber());
                wrongAnswer.setQuestionNumber(question.getQuestionNumber());
                wrongAnswer.setCorrectAnswerIndex(correct);
                wrongAnswer.setUserAnswerIndex(user);
                if (question.getQuestHelp() != null) {
                    wrongAnswer.setExplanation(question.getQuestHelp());
                }
                if (question.getTopic() != null) {
                    wrongAnswer.setTopicName(question.getTopic().getTopicName());
                }
                wrongAnswers.add(wrongAnswer);
            }
        }


        return wrongAnswers;
    }

}
