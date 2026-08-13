package com.pdd.pddai.entity;

import com.pdd.pddai.util.IntegerListConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "user_attempts", indexes = {
        @Index(name = "idx_user_attempts_user_ticket", columnList = "user_id, ticket_number"),
        @Index(name = "idx_user_attempts_user_date", columnList = "user_id, attempt_date DESC")
})
public class UserAttemptsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "ticket_number", nullable = false)
    private  Integer ticketNumber;

    @CreationTimestamp
    @Column(name = "attempt_date",nullable = false, updatable = false)
    private LocalDateTime attemptDate;

    @Column(name = "wrong_count", nullable = false)
    private  Integer wrongCount;

    @Convert(converter = IntegerListConverter.class)
    @Column(name = "wrong_topic_ids", nullable = false, columnDefinition = "TEXT")
    private List<Integer> wrongTopicIds;

}
