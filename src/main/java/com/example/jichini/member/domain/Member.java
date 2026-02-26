package com.example.jichini.member.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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
    private String province;
    private String city;
    private Integer age;
    private String concern;
    private String concernDetail;
    private String emotion;
    @Column(columnDefinition = "LONGTEXT")
    private String profileImage;  // Base64 이미지
    private String tags;          // 태그 샵 형태
}
