package com.opt.ssafy.optback.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMessageToMember {

    private int receiverId;
    private String content;
    private int senderId;
}
