package com.example.jichini.member.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Pattern(regexp = "^[a-zA-Z0-9]{3,15}$", message = "아이디는 영문과 숫자를 포함한 3~15자여야 합니다.")
    @Column(nullable = false, unique = true)
    private String userId;

    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "비밀번호는 영문과 숫자를 포함하여 8글자 이상이어야 합니다."
    )
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String job;
    private String province;
    private String city;
    private Integer age;
    private String concern;

    @Column(nullable = false)
    private String concernDetail;
    private String emotion;
    @Column(columnDefinition = "LONGTEXT")
    private String profileImage;  // Base64 이미지
    private String tags;          // 태그 샵 형태
}
