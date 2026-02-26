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

import java.util.HashMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();
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
                .province(dto.getProvince())
                .city(dto.getCity())
                .age(dto.getAge())
                .concern(dto.getConcern())
                .concernDetail(dto.getConcernDetail())
                .emotion(dto.getEmotion())
                .profileImage(dto.getProfileImage())
                .tags(dto.getTags())
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
                                "province", dto.getProvince() != null ? dto.getProvince() : "",
                                "city", dto.getCity() != null ? dto.getCity() : "",
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

    @Transactional
    public Member updateMyInfo(String token, Map<String, Object> body) {
        String userId = jwtTokenProvider.getUserId(token);
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // Setter로 직접 수정 (JPA dirty checking 발동!)
        if (body.containsKey("name") && body.get("name") != null) {
            member.setName(body.get("name").toString());
        }
        if (body.containsKey("job") && body.get("job") != null) {
            member.setJob(body.get("job").toString());
        }
        if (body.containsKey("province") && body.get("province") != null) {
            member.setProvince(body.get("province").toString());
        }
        if (body.containsKey("city") && body.get("city") != null) {
            member.setCity(body.get("city").toString());
        }
        if (body.containsKey("age") && body.get("age") != null) {
            member.setAge(Integer.parseInt(body.get("age").toString()));
        }
        if (body.containsKey("concern") && body.get("concern") != null) {
            member.setConcern(body.get("concern").toString());
        }
        if (body.containsKey("concernDetail") && body.get("concernDetail") != null) {
            member.setConcernDetail(body.get("concernDetail").toString());
        }
        if (body.containsKey("emotion") && body.get("emotion") != null) {
            member.setEmotion(body.get("emotion").toString());
        }
        if (body.containsKey("profileImage") && body.get("profileImage") != null) {
            member.setProfileImage(body.get("profileImage").toString());
        }
        if (body.containsKey("tags") && body.get("tags") != null) {
            member.setTags(body.get("tags").toString());
        }

        if (member.getProvince() != null && member.getConcern() != null) {
            try {
                Map<String, Object> pineconeBody = new HashMap<>();
                pineconeBody.put("user_id", userId);
                pineconeBody.put("province", member.getProvince());
                pineconeBody.put("city", member.getCity() != null ? member.getCity() : "");
                pineconeBody.put("concern", member.getConcern());

                restTemplate.put("http://localhost:5000/embed-user", pineconeBody);
            } catch (Exception e) {
                // Pinecone 실패해도 MySQL은 정상 저장
                System.out.println("Pinecone 업데이트 실패: " + e.getMessage());
            }
        }
        // save() 없어도 @Transactional이 끝날 때 자동 UPDATE!
        return member;
    }

    public String login(MemberLoginReqDto dto) {
        Member member = memberRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(member.getUserId());
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        memberRepository.delete(member);
    }

}