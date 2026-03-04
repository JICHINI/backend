// ChatRoomRepository.java
package com.example.jichini.chatroom.repository;

import com.example.jichini.chatroom.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 두 사람 사이 방이 이미 있는지 확인
    Optional<ChatRoom> findByUserAAndUserB(String userA, String userB);
    Optional<ChatRoom> findByUserBAndUserA(String userA, String userB);
    // 내가 참여한 방 목록
    List<ChatRoom> findByUserAOrUserB(String userA, String userB);
    void deleteByUserAOrUserB(String userA, String userB);
}