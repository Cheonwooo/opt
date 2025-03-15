package com.opt.ssafy.optback.domain.ticket.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequest {

    private int id;
    private int price;
    private int totalSessions;
    private LocalDate startDate;
    private LocalDate lastUsedDate;
    private int usedSessions;
    private String status;

}
