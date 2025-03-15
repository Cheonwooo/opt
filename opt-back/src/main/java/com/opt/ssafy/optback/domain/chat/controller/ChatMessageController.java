package com.opt.ssafy.optback.domain.chat.controller;

import com.opt.ssafy.optback.domain.chat.dto.ChatMessageRequest;
import com.opt.ssafy.optback.domain.chat.entity.ChatMessage;
import com.opt.ssafy.optback.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // 1:1 채팅 메시지 (roomId 기반)
    @MessageMapping("/chat-room/{roomId}")
    public void sendMessage(ChatMessageRequest messageRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || authentication.getName() == null) {
//            throw new ChatMessageException("❌ 인증되지 않은 사용자입니다.");
//        }
        ChatMessage savedMessage = chatMessageService.processMessage(messageRequest);
    }
}
