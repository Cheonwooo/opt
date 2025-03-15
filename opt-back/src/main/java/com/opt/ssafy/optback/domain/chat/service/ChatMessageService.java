package com.opt.ssafy.optback.domain.chat.service;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.chat.dto.ChatMessageRequest;
import com.opt.ssafy.optback.domain.chat.entity.ChatMessage;
import com.opt.ssafy.optback.domain.chat.entity.ChatRoom;
import com.opt.ssafy.optback.domain.chat.exception.ChatMessageException;
import com.opt.ssafy.optback.domain.chat.exception.ChatRoomException;
import com.opt.ssafy.optback.domain.chat.repository.ChatMessageRepository;
import com.opt.ssafy.optback.domain.chat.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // 메시지 저장 및 전송
    @Transactional
    public ChatMessage processMessage(ChatMessageRequest request) {
        // 채팅방 ID 확인
        if (request.getRoomId() == null || request.getRoomId().isEmpty()) {
            throw new ChatRoomException("❌ 채팅방 ID가 필요합니다");
        }

        // 로그인한 유저 아이디 가져오기
        int senderId;
        try {
            senderId = userDetailsService.getMemberByContextHolder().getId();
        } catch (Exception e) {
            throw new ChatMessageException("❌ 인증되지 않은 사용자입니다");
        }

        boolean isAdmin = (senderId == 0);

        String roomName = chatRoomRepository.findById(request.getRoomId())
                .map(ChatRoom::getRoomName)
                .orElse(null);

        boolean isAdminChatRoom = roomName.startsWith("0_");

        if (!isAdmin && isAdminChatRoom) {
            throw new ChatMessageException("🚫 일반 유저는 관리자 채팅에 메시지를 보낼 수 없습니다");
        }
        
        int receiverId = getReceiverId(request.getRoomId(), senderId);

        log.info("📩 메시지 전송 요청: Room ID = {}, Sender ID = {}, Receiver ID = {}", request.getRoomId(), senderId,
                receiverId);

        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(request.getRoomId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .messageType(ChatMessage.MessageType.CHAT)
                .isRead(false)
                .build();

        // 메시지 저장
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("📩 메시지 저장 완료: Message ID = {}", savedMessage.getId());

        // 메시지 전송
        sendMessageToChatRoom(savedMessage);
        return savedMessage;
    }

    private void sendMessageToChatRoom(ChatMessage message) {
        simpMessagingTemplate.convertAndSend(
                "/topic/chat-room/" + message.getRoomId(), message
        );
    }

    private int getReceiverId(String roomId, int senderId) {
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(roomId);

        if (optionalChatRoom.isEmpty()) {
            throw new ChatRoomException("❌ 존재하지 않는 채팅방입니다");
        }

        ChatRoom chatRoom = optionalChatRoom.get();
        List<Integer> participants = chatRoom.getParticipants();

        return participants.stream()
                .filter(id -> id != senderId)
                .findFirst()
                .orElseThrow(() -> new ChatMessageException("❌ 채팅방에 상대방이 존재하지 않습니다"));
    }

    // 각 채팅방 메시지 조회
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        log.info("📩 메시지 조회 요청: Room ID = {}", roomId);
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    // 채팅방의 모든 메시지를 읽음 처리
    @Transactional
    public void markAllMessagesAsRead(String roomId) {
        int memberId = userDetailsService.getMemberByContextHolder().getId();

        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByRoomIdAndReadByMembersNotContaining(roomId, memberId);

        if (unreadMessages.isEmpty()) {
            return;
        }

        for (ChatMessage message : unreadMessages) {
            message.getReadByMembers().add(memberId);
            message.setIsRead(checkIfAllRead(message));
        }

        chatMessageRepository.saveAll(unreadMessages);

        simpMessagingTemplate.convertAndSend(
                "/topic/chat-room/" + roomId + "/read-status",
                unreadMessages
        );
    }

    // 모든 참여자가 읽었는지 확인
    private boolean checkIfAllRead(ChatMessage message) {
        int totalParticipants = getTotalParticipants(message.getRoomId());
        return message.getReadByMembers().size() >= totalParticipants;
    }

    private int getTotalParticipants(String roomId) {
        return chatRoomRepository.findById(roomId).get().getParticipants().size();
    }

}
