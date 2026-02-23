package com.example.jichini.member.service;

import com.example.jichini.common.auth.JwtTokenProvider;
import com.example.jichini.member.domain.Member;
import com.example.jichini.member.domain.Role;
import com.example.jichini.member.dto.MemberLoginReqDto;
import com.example.jichini.member.dto.MemberSaveReqDto;
import com.example.jichini.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient = WebClient.create("http://localhost:5000");

    @Transactional
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

        Member saved = memberRepository.save(member);

        // Pinecone에 임베딩 저장 (고민 상세 우선, 없으면 카테고리)
        String concernText = (dto.getConcernDetail() != null && !dto.getConcernDetail().isBlank())
                ? dto.getConcernDetail()
                : dto.getConcern();

        if (concernText != null && !concernText.isBlank()) {
            try {
                webClient.post()
                        .uri("/embed-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "user_id", dto.getUserId(),
                                "province", dto.getLocation() != null ? dto.getLocation() : "",
                                "city", "",
                                "concern", concernText
                        ))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .subscribe(
                                res -> System.out.println("Pinecone 저장 완료: " + res),
                                err -> System.out.println("Pinecone 저장 실패 (무시): " + err.getMessage())
                        );
            } catch (Exception e) {
                System.out.println("Pinecone 저장 실패 (무시): " + e.getMessage());
            }
        }

        return saved;
    }

    public String login(MemberLoginReqDto dto) {
        Member member = memberRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(member.getUserId());
    }
}