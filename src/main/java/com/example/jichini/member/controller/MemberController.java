package com.example.jichini.member.controller;

import com.example.jichini.member.domain.Member;
import com.example.jichini.member.dto.MemberLoginReqDto;
import com.example.jichini.member.dto.MemberSaveReqDto;
import com.example.jichini.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto) {
        Member member = memberService.create(memberSaveReqDto);
        return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginReqDto memberLoginReqDto) {
        String token = memberService.login(memberLoginReqDto);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }
}
