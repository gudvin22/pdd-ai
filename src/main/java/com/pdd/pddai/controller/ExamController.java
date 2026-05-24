package com.pdd.pddai.controller;

import com.pdd.pddai.dto.ExamCheckRequestDto;
import com.pdd.pddai.dto.ExamResponseDto;
import com.pdd.pddai.dto.QuestionResponseDto;
import com.pdd.pddai.dto.WrongAnswerDto;
import com.pdd.pddai.exception.TicketNotFoundException;
import com.pdd.pddai.repository.QuestionRepository;
import com.pdd.pddai.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;



    @GetMapping("/random")
    public ResponseEntity<ExamResponseDto> getRandomTicket() {
        int randomTicket = (int) (Math.random() * 40) + 1;

        try {
            List<QuestionResponseDto> questions = examService.getTicketNumber(randomTicket);
            ExamResponseDto examResponseDto = new ExamResponseDto();
            examResponseDto.setQuestions(questions);
            examResponseDto.setTicketNumber(randomTicket);
            return ResponseEntity.ok(examResponseDto);

        } catch (TicketNotFoundException e) {
            // если билета нет (хотя при random от 1 до 40 такого быть не может, но на будущее)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            // все другие неожиданные ошибки
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/check")
    public ResponseEntity<List<WrongAnswerDto>> checkTicket(@RequestBody ExamCheckRequestDto examCheckRequestDto) {
        try {
            return ResponseEntity.ok(examService.checkExam(examCheckRequestDto));

        } catch (TicketNotFoundException e) {
            // если билета нет (хотя при random от 1 до 40 такого быть не может, но на будущее)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        }  catch (Exception e) {
            // все другие неожиданные ошибки
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }


}
