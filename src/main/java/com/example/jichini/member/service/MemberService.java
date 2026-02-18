package com.example.jichini.member.service;

import com.example.jichini.member.domain.Member;
import com.example.jichini.member.domain.Role;
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
    private final PasswordEncoder passwordEncoder;  // 추가

    public Member create(MemberSaveReqDto dto) {
        if (memberRepository.findByUserId(dto.getUserId()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Member member = Member.builder()
                .name(dto.getName())
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))  // 해시화
                .role(Role.USER)
                .build();

        return memberRepository.save(member);
    }
}

