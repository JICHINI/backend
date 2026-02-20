package com.example.jichini.member.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSaveReqDto {
    private String name;
    private String userId;
    private String password;

    private String job;
    private String location;
    private Integer age;
    private String concern;
    private String concernDetail;
    private String emotion;
}

