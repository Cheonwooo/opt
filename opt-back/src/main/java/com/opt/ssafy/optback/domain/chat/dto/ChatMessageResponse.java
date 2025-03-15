package com.opt.ssafy.optback.domain.chat.dto;

import com.opt.ssafy.optback.domain.chat.entity.ChatMessage;
import com.opt.ssafy.optback.domain.chat.entity.ChatMessage.MessageType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private String id;
    private int senderId;
    private int receiverId;
    private String roomId;
    private String content;
    private LocalDateTime createdAt;
    private MessageType messageType;
    private Boolean isRead;
    private List<Integer> readByMembers = new ArrayList<>();

    public ChatMessageResponse(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.senderId = chatMessage.getSenderId();
        this.receiverId = chatMessage.getReceiverId();
        this.roomId = chatMessage.getRoomId();
        this.content = chatMessage.getContent();
        this.createdAt = chatMessage.getCreatedAt();
        this.messageType = chatMessage.getMessageType();
        this.isRead = chatMessage.getIsRead();
        this.readByMembers =
                chatMessage.getReadByMembers() != null ? chatMessage.getReadByMembers() : new ArrayList<>();

    }
}
