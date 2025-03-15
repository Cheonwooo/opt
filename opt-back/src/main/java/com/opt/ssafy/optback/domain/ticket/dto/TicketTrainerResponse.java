package com.opt.ssafy.optback.domain.ticket.dto;

import com.opt.ssafy.optback.domain.session.dto.SessionResponse;
import com.opt.ssafy.optback.domain.ticket.entity.Ticket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketTrainerResponse {

    private int id;
    private int trainerId;
    private String trainerName;
    private int studentId;
    private String studentName;
    private int price;
    private int totalSessions;
    private LocalDate startDate;
    private LocalDate lastUsedDate;
    private int usedSessions;
    private String status;
    private List<SessionResponse> sessions;

    public TicketTrainerResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.trainerId = ticket.getTrainer().getId();
        this.trainerName = ticket.getTrainer().getName();
        this.studentId = ticket.getStudent().getId();
        this.studentName = ticket.getStudent().getName();
        this.price = ticket.getPrice();
        this.totalSessions = ticket.getTotalSessions();
        this.startDate = ticket.getStartDate();
        this.lastUsedDate = ticket.getLastUsedDate();
        this.usedSessions = ticket.getUsedSessions();
        this.status = ticket.getStatus();
        this.sessions = Optional.ofNullable(ticket.getSessions())
                .orElseGet(ArrayList::new)
                .stream()
                .map(session -> SessionResponse.builder()
                        .id(session.getId())
                        .ticketId(session.getTicket().getId())
                        .number(session.getNumber())
                        .startAt(session.getStartAt())
                        .endAt(session.getEndAt())
                        .isMemberSigned(session.isMemberSigned())
                        .isTrainerSigned(session.isTrainerSigned())
                        .build())
                .collect(Collectors.toList());

    }

}
