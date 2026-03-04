package com.example.jichini.member.controller;

import com.example.jichini.common.auth.JwtTokenProvider;
import com.example.jichini.member.domain.Member;
import com.example.jichini.member.dto.MemberLoginReqDto;
import com.example.jichini.member.dto.MemberSaveReqDto;
import com.example.jichini.member.repository.MemberRepository;
import com.example.jichini.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;


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

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String token) {
        String userId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        return ResponseEntity.ok(member);
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<?> updateMyInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body
    ) {
        return ResponseEntity.ok(memberService.updateMyInfo(token.replace("Bearer ", ""), body));
    }

    // 다른 유저 프로필 조회
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        return memberRepository.findByUserId(userId)
                .map(member -> ResponseEntity.ok(Map.of(
                        "userId", member.getUserId(),
                        "name", member.getName(),
                        "profileImage", member.getProfileImage() != null ? member.getProfileImage() : "",
                        "province", member.getProvince() != null ? member.getProvince() : "",
                        "city", member.getCity() != null ? member.getCity() : ""
                )))
                .orElse(ResponseEntity.ok(Map.of("userId", userId, "name", "", "profileImage", "", "province", "", "city", "")));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMember(
            @RequestHeader("Authorization") String token
    ) {
        memberService.deleteMember(token.replace("Bearer ", ""));
        return ResponseEntity.ok("탈퇴 완료");
    }
}
