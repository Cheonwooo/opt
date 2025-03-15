package com.opt.ssafy.optback.domain.chat.controller;

import com.opt.ssafy.optback.domain.chat.dto.SystemMessageToMember;
import com.opt.ssafy.optback.domain.chat.dto.SystemMessageToRoom;
import com.opt.ssafy.optback.domain.chat.service.SystemMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chat-messages/system")
@RequiredArgsConstructor
public class SystemMessageController {

    private final SystemMessageService systemMessageService;

    // 특정 멤버에게 메시지 전송
    @PostMapping("/member")
    public ResponseEntity<String> sendSystemMessageToUser(@RequestBody SystemMessageToMember request) {
        log.info("📩 [API] 특정 유저({})에게 시스템 메시지 요청: {}", request.getReceiverId(), request.getContent());
        systemMessageService.sendSystemMessageToMember(request);
        return ResponseEntity.ok("📩 시스템 메시지를 특정 유저에게 성공적으로 전송했습니다.");
    }

    // 특정 채팅방의 모든 멤버에게 시스템 메시지 전송 (예: 채팅방 입장/퇴장)
    @PostMapping("/room")
    public ResponseEntity<String> sendSystemMessageToRoom(@RequestBody SystemMessageToRoom request) {
        log.info("📩 [API] 채팅방({}) 전체에게 시스템 메시지 요청: {}", request.getRoomId(), request.getContent());
        systemMessageService.sendSystemMessageToRoom(request);
        return ResponseEntity.ok("📩 시스템 메시지를 채팅방 전체에게 성공적으로 전송했습니다.");
    }
}
