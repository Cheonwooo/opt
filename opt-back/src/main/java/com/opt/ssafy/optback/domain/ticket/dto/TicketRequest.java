package com.opt.ssafy.optback.domain.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {

    private String studentEmail;
    private int price;
    private int totalSessions;

}
