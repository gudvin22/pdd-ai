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

            public List<QuestionResponseDto> getTicketNumber (int ticketNumber) {

                List<QuestionEntity> questions = questionRepository.findByTicketNumber(ticketNumber);
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

        List<QuestionEntity> questions = questionRepository.findByTicketNumber(request.getTicketNumber());
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
                WrongAnswerDto wrongAnswer = new WrongAnswerDto();
                wrongAnswer.setQuestionNumber(i + 1);
                wrongAnswer.setCorrectAnswerIndex(correct);
                wrongAnswer.setUserAnswerIndex(user);
                wrongAnswers.add(wrongAnswer);
            }
        }

        return wrongAnswers;
    }

}
