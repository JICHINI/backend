package com.example.jichini.member.dto;

import lombok.Data;

@Data
public class MemberLoginReqDto {
    private String userId;
    private String password;
}