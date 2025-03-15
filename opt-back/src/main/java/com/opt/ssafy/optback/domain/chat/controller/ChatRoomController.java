package com.opt.ssafy.optback.domain.chat.controller;

import com.opt.ssafy.optback.domain.chat.dto.ChatMessageResponse;
import com.opt.ssafy.optback.domain.chat.dto.ChatRoomResponse;
import com.opt.ssafy.optback.domain.chat.entity.ChatRoom;
import com.opt.ssafy.optback.domain.chat.service.ChatMessageService;
import com.opt.ssafy.optback.domain.chat.service.ChatRoomService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("chat-rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    // 채팅방 생성 (또는 기존 채팅방 반환)
    @PostMapping("/create")
    public ResponseEntity<ChatRoomResponse> getOrCreateRoom(@RequestParam int otherMemberId) {
        ChatRoom chatRoom = chatRoomService.getOrCreateRoom(otherMemberId);
        return ResponseEntity.ok(new ChatRoomResponse(chatRoom));
    }

    // 사용자의 채팅방 목록 조회
    @GetMapping("/list")
    public ResponseEntity<Page<ChatRoomResponse>> getUserRooms(Pageable pageable) {
        List<ChatRoomResponse> chatRooms = chatRoomService.getUserChatRooms();

        return ResponseEntity.ok(convertListToPage(chatRooms, pageable));
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/message")
    public ResponseEntity<Page<ChatMessageResponse>> getChatRoomMessages(@RequestParam String roomId,
                                                                         Pageable pageable) {
        List<ChatMessageResponse> messages = chatMessageService.getMessagesByRoomId(roomId)
                .stream()
                .map(ChatMessageResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(convertListToPage(messages, pageable));
    }

    @PostMapping("/leave/{roomId}")
    public ResponseEntity<String> leaveChatRoom(@PathVariable String roomId) {
        chatRoomService.leaveChatRoom(roomId);
        return ResponseEntity.ok("채팅방 나가기 성공");
    }

    @PostMapping("/read-all/{roomId}")
    public ResponseEntity<String> markAllMessagesAsRead(@PathVariable String roomId) {
        chatMessageService.markAllMessagesAsRead(roomId);
        return ResponseEntity.ok("채팅방의 모든 메시지를 읽음 처리하였습니다.");
    }

    // Pageable 변환용 메서드
    private <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }
}
