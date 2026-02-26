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
    private String province;            //지역
    private String city;                //도시 시/군
    private Integer age;
    private String concern;
    private String concernDetail;
    private String emotion;
    private String profileImage;
    private String tags;


}

