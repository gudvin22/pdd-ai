package com.pdd.pddai.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "topics")
public class TopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_name")
    private String topicName;
}
