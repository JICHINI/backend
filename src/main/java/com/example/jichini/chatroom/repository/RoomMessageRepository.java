// RoomMessageRepository.java
package com.example.jichini.chatroom.repository;

import com.example.jichini.chatroom.domain.RoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomMessageRepository extends JpaRepository<RoomMessage, Long> {
    List<RoomMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
    void deleteBySenderId(String senderId);

    // 특정 방의 안읽은 메시지 수 (내가 보낸 건 제외)
    int countByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, String myId);

    // 특정 방의 안읽은 메시지 목록 (읽음 처리용)
    List<RoomMessage> findByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, String myId);
}