package com.opt.ssafy.optback.domain.chat.service;

import com.opt.ssafy.optback.domain.chat.dto.SystemMessageToMember;
import com.opt.ssafy.optback.domain.chat.dto.SystemMessageToRoom;
import com.opt.ssafy.optback.domain.chat.entity.ChatMessage;
import com.opt.ssafy.optback.domain.chat.entity.ChatRoom;
import com.opt.ssafy.optback.domain.chat.exception.ChatRoomException;
import com.opt.ssafy.optback.domain.chat.repository.ChatMessageRepository;
import com.opt.ssafy.optback.domain.chat.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    // 시스템 메시지 방 전체 인원에게 전송
    @Transactional
    public void sendSystemMessageToRoom(SystemMessageToRoom request) {
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(request.getRoomId());
        if (chatRoomOptional.isEmpty()) {
            throw new ChatRoomException("❌ 존재하지 않는 채팅방입니다");
        }
        ChatMessage systemMessage = createSystemMessage(request.getRoomId(), request.getSenderId(),
                request.getContent());
        chatMessageRepository.save(systemMessage);
        simpMessagingTemplate.convertAndSend("/topic/chat-room/" + request.getRoomId(), systemMessage);
        log.info("📩 [System] 채팅방({}) 내 전체 유저에게 시스템 메시지 전송: {}", request.getRoomId(), request.getContent());
    }

    @Transactional
    public void sendSystemMessageToMember(SystemMessageToMember request) {
        String roomId = findOrCreateSystemRoom(request.getReceiverId());

        ChatMessage systemMessage = createSystemMessage(roomId, request.getSenderId(), request.getContent());
        chatMessageRepository.save(systemMessage);

        simpMessagingTemplate.convertAndSend("/topic/chat-room/" + roomId, systemMessage);
        log.info("📩 [System] 특정 유저({})에게 시스템 메시지 전송: {}", request.getReceiverId(), request.getContent());
    }

    private ChatMessage createSystemMessage(String roomId, int senderId, String content) {
        return ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .messageType(ChatMessage.MessageType.SYSTEM)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    private String findOrCreateSystemRoom(int receiverId) {
        // 관리자는 ID 0으로 가정
        int adminId = 0;
        String roomId = ChatRoom.generateRoomName(adminId, receiverId);

        Optional<ChatRoom> existingRoom = chatRoomRepository.findById(roomId);
        if (existingRoom.isPresent()) {
            return roomId;
        }

        // 새로운 채팅방 생성
        ChatRoom newRoom = ChatRoom.builder()
                .id(roomId)
                .participants(java.util.List.of(adminId, receiverId))
                .build();

        chatRoomRepository.save(newRoom);
        log.info("📩 [System] 새로운 시스템 전용 채팅방 생성: {}", roomId);
        return roomId;
    }

}
