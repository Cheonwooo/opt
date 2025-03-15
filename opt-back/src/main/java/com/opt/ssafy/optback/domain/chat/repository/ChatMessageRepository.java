package com.opt.ssafy.optback.domain.chat.repository;

import com.opt.ssafy.optback.domain.chat.entity.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Integer> {

    // 채팅방 메시지를 오래된 순으로 조회
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId);

    // 가장 최근에 보낸 메시지 1개 조회
    Optional<ChatMessage> findTopByRoomIdOrderByCreatedAtDesc(String roomId);

    // 특정 채팅방에서 특정 사용자가 읽지 않은 메시지 조회
    List<ChatMessage> findByRoomIdAndReadByMembersNotContaining(String roomId, int memberId);

}
