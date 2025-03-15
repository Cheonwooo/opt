package com.opt.ssafy.optback.domain.chat.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomRequest {

    private String id;
    private String roomName;
    private List<Integer> participants;

}
