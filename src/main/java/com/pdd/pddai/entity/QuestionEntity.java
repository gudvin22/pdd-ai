package com.pdd.pddai.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false, length = 3)
    private String category;

    @Column(name = "ticket_number", nullable = false)
    private  Integer ticketNumber;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "correct_answer_index", nullable = false)
    private  Integer correctAnswerIndex;

    @Column(name = "answers_text", columnDefinition = "TEXT[]")
    private List<String> answersText;

    @Column(name = "image_url_small", length = 500)
    private String imageUrlSmall;

    @Column(name = "image_url_big", length = 500)
    private String imageUrlBig;




}
