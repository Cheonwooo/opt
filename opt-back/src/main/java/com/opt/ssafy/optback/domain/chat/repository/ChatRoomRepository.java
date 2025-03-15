package com.opt.ssafy.optback.domain.chat.repository;

import com.opt.ssafy.optback.domain.chat.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomRepository extends
        MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByParticipantsContaining(int memberId);

    List<ChatRoom> findByRoomName(String roomName);

    Optional<ChatRoom> findById(String id);
}
