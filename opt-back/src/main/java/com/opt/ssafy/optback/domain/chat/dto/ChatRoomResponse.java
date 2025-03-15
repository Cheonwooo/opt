package com.opt.ssafy.optback.domain.chat.dto;

import com.opt.ssafy.optback.domain.chat.entity.ChatMessage;
import com.opt.ssafy.optback.domain.chat.entity.ChatRoom;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    private String id;
    private String roomName;
    private List<Integer> participants;
    private String otherMemberNickname;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    public ChatRoomResponse(ChatRoom chatRoom, String otherMemberNickname, ChatMessage chatMessage) {
        this.id = chatRoom.getId();
        this.roomName = chatRoom.getRoomName();
        this.participants = chatRoom.getParticipants();
        this.otherMemberNickname = otherMemberNickname;
        this.lastMessage = chatMessage.getContent();
        this.lastMessageTime = chatMessage.getCreatedAt();
    }

    public ChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.roomName = chatRoom.getRoomName();
        this.participants = chatRoom.getParticipants();
        this.otherMemberNickname = null;
        this.lastMessage = null;
    }
}
