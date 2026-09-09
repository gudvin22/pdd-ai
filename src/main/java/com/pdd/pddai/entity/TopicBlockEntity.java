package com.pdd.pddai.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "topic_blocks")
public class TopicBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private TopicEntity topic;

    @Column(name = "block_number", nullable = false)
    private Integer blockNumber;


}
