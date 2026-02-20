package com.example.jichini.member.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String userId;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String job;
    private String location;
    private Integer age;
    private String concern;
    private String concernDetail;
    private String emotion;
}
