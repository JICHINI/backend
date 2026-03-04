package com.example.jichini.chatroom.controller;

import com.example.jichini.chatroom.domain.ChatRoom;
import com.example.jichini.chatroom.domain.RoomMessage;
import com.example.jichini.chatroom.repository.ChatRoomRepository;
import com.example.jichini.chatroom.repository.RoomMessageRepository;
import com.example.jichini.common.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class RoomChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final RoomMessageRepository roomMessageRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 채팅방 생성 or 기존 방 반환
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        String myId = extractUserId(authHeader);
        String targetId = body.get("targetUserId");

        // 이미 방이 있으면 그 방 반환
        Optional<ChatRoom> existing = chatRoomRepository.findByUserAAndUserB(myId, targetId);
        if (existing.isEmpty()) existing = chatRoomRepository.findByUserBAndUserA(myId, targetId);

        ChatRoom room = existing.orElseGet(() ->
                chatRoomRepository.save(ChatRoom.builder()
                        .userA(myId)
                        .userB(targetId)
                        .build())
        );

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_CHAT_REQUEST");
        notification.put("roomId", room.getId());
        notification.put("from", myId);
        notification.put("timestamp", System.currentTimeMillis());

        // 명시적 타입 캐스팅으로 오류 해결
        messagingTemplate.convertAndSend("/sub/user/" + targetId, (Object) notification);

        return ResponseEntity.ok(Map.of("roomId", room.getId()));
    }

    // 내 채팅방 목록
    @GetMapping("/rooms")
    public ResponseEntity<?> getRooms(@RequestHeader("Authorization") String authHeader) {
        String myId = extractUserId(authHeader);
        List<ChatRoom> rooms = chatRoomRepository.findByUserAOrUserB(myId, myId);
        return ResponseEntity.ok(rooms);
    }

    // 채팅방 메시지 기록
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId));
    }

    // STOMP 메시지 수신 → 저장 → 브로드캐스트
    @MessageMapping("/chat/{roomId}")
    public void handleMessage(
            @DestinationVariable Long roomId,
            @Payload Map<String, String> payload
    ) {
        String senderId = payload.get("senderId");
        String content = payload.get("content");

        RoomMessage saved = roomMessageRepository.save(RoomMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .build());

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("id", saved.getId());
        messageMap.put("roomId", roomId);
        messageMap.put("senderId", senderId);
        messageMap.put("content", content);
        messageMap.put("createdAt", saved.getCreatedAt().toString());

        // 명시적 타입 캐스팅
        messagingTemplate.convertAndSend("/sub/chat/" + roomId, (Object) messageMap);
    }

    private String extractUserId(String authHeader) {
        return jwtTokenProvider.getUserId(authHeader.replace("Bearer ", ""));
    }
}