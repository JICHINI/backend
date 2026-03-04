package com.example.jichini.chat.controller;

import com.example.jichini.chat.domain.ChatMessage;
import com.example.jichini.chat.dto.ChatRequestDto;
import com.example.jichini.chat.repository.ChatMessageRepository;
import com.example.jichini.common.auth.JwtTokenProvider;
import com.example.jichini.member.domain.Member;
import com.example.jichini.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class
ChatController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebClient webClient = WebClient.create("http://localhost:5000");

    // 채팅 기록 조회
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @RequestHeader("Authorization") String authHeader
    ) {
        String userId = extractUserId(authHeader);
        List<ChatMessage> history = chatMessageRepository.findByUserIdOrderByCreatedAtAsc(userId);
        return ResponseEntity.ok(history);
    }

    // AI 채팅 + 기록 저장
    @PostMapping
    public ResponseEntity<Map> chat(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChatRequestDto req
    ) {
        String userId = extractUserId(authHeader);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String province = member.getProvince() != null ? member.getProvince() : "";
        String city = member.getCity() != null ? member.getCity() : "";

        // 사용자 메시지 저장
        chatMessageRepository.save(ChatMessage.builder()
                .userId(userId)
                .sender("user")
                .content(req.getMessage())
                .build());

        // FastAPI 호출
        Map response = webClient.post()
                .uri("/chat/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "message", req.getMessage(),
                        "user_province", province,
                        "user_city", "",
                        "session_id", userId
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String answer = response != null ? (String) response.get("answer") : "응답을 받지 못했습니다.";

        // AI 응답 저장
        chatMessageRepository.save(ChatMessage.builder()
                .userId(userId)
                .sender("bot")
                .content(answer)
                .build());

        return ResponseEntity.ok(Map.of("answer", answer));
    }



    private String extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserId(token);
    }
}
