package com.opt.ssafy.optback.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMessageToRoom {

    private String roomId;
    private String content;
    private int senderId;

}
