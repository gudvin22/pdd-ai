package com.pdd.pddai.entity;

import com.pdd.pddai.enums.RoleUser;
import com.pdd.pddai.enums.SubscriptionTypeUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", unique = true, nullable = false)
    private String telegramId;

    @Column(name = "email")
    private  String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "user_name")
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleUser role = RoleUser.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription", nullable = false)
    private SubscriptionTypeUser subscription = SubscriptionTypeUser.PREMIUM;

    @Column(name = "registration_date",nullable = false, updatable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Column(name = "last_active_date",nullable = false)
    private LocalDateTime lastActiveDate = LocalDateTime.now();

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;


}
