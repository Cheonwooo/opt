package com.opt.ssafy.optback.domain.chat.entity;

import jakarta.persistence.Id;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_rooms")
public class ChatRoom {

    @Id
    private String id;
    private String roomName;
    private List<Integer> participants;

    public void setParticipants(List<Integer> participants) {
        this.participants = participants;
    }

    public static String generateRoomName(int user1, int user2) {
        return user1 < user2 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

}
