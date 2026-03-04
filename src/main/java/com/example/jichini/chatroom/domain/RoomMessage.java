package com.example.jichini.chatroom.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private String senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @Builder.Default
    private boolean isRead = false; // 읽은지 안 읽은지

    public void markAsRead() { this.isRead = true; } //읽음 처리

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}