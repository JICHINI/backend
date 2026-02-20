package com.example.jichini.member.service;

import com.example.jichini.common.auth.JwtTokenProvider;
import com.example.jichini.member.domain.Member;
import com.example.jichini.member.domain.Role;
import com.example.jichini.member.dto.MemberLoginReqDto;
import com.example.jichini.member.dto.MemberSaveReqDto;
import com.example.jichini.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Member create(MemberSaveReqDto dto) {
        if (memberRepository.findByUserId(dto.getUserId()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Member member = Member.builder()
                .name(dto.getName())
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .job(dto.getJob())
                .location(dto.getLocation())
                .age(dto.getAge())
                .concern(dto.getConcern())
                .concernDetail(dto.getConcernDetail())
                .emotion(dto.getEmotion())
                .build();

        return memberRepository.save(member);
    }

    // 로그인
    public String login(MemberLoginReqDto dto) {
        Member member = memberRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(member.getUserId());
    }
}

