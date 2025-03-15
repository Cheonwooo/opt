package com.opt.ssafy.optback.domain.chat.entity;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;
    private int senderId;
    private int receiverId;
    private String roomId;
    private String content;
    private LocalDateTime createdAt;
    private MessageType messageType;
    private Boolean isRead = false;
    private List<Integer> readByMembers = new ArrayList<>();

    public enum MessageType {
        CHAT, SYSTEM
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
