// RoomMessageRepository.java
package com.example.jichini.chatroom.repository;

import com.example.jichini.chatroom.domain.RoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomMessageRepository extends JpaRepository<RoomMessage, Long> {
    List<RoomMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
}