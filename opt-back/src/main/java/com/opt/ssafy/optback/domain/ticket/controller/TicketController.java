package com.opt.ssafy.optback.domain.ticket.controller;

import com.opt.ssafy.optback.domain.ticket.dto.TicketRequest;
import com.opt.ssafy.optback.domain.ticket.dto.TicketStudentResponse;
import com.opt.ssafy.optback.domain.ticket.dto.TicketTrainerResponse;
import com.opt.ssafy.optback.domain.ticket.dto.UpdateTicketRequest;
import com.opt.ssafy.optback.domain.ticket.entity.Ticket;
import com.opt.ssafy.optback.domain.ticket.service.TicketService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("trainer-used")
    public ResponseEntity<Page<TicketTrainerResponse>> getTicketsByTrainerAndUsed(Pageable pageable) {
        List<TicketTrainerResponse> response = ticketService.getTicketsByTrainerIdAndIsUsed().stream()
                .map(TicketTrainerResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(convertListToPage(response, pageable));
    }

    @GetMapping("student-used")
    public ResponseEntity<Page<TicketStudentResponse>> getTicketsByStudentAndUsed(Pageable pageable) {
        List<TicketStudentResponse> response = ticketService.getTicketsByStudentIdAndIsUsed().stream()
                .map(TicketStudentResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(convertListToPage(response, pageable));
    }

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("trainer-not-used")
    public ResponseEntity<Page<TicketTrainerResponse>> getTicketsByTrainerAndNotUsed(Pageable pageable) {
        List<TicketTrainerResponse> response = ticketService.getTicketsByTrainerIdAndIsNotUsed().stream()
                .map(TicketTrainerResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(convertListToPage(response, pageable));
    }

    @GetMapping("student-not-used")
    public ResponseEntity<Page<TicketStudentResponse>> getTicketsByStudentAndNotUsed(Pageable pageable) {
        List<TicketStudentResponse> response = ticketService.getTicketsByStudentIdAndIsNotUsed().stream()
                .map(TicketStudentResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(convertListToPage(response, pageable));
    }

    @PostMapping
    public ResponseEntity<TicketTrainerResponse> addTicket(@RequestBody TicketRequest ticketRequest) {
        Ticket ticket = ticketService.saveTicket(ticketRequest);
        return ResponseEntity.ok(new TicketTrainerResponse(ticket));
    }

    @PatchMapping
    public ResponseEntity<TicketTrainerResponse> updateTicket(@RequestBody UpdateTicketRequest ticketRequest) {
        Ticket update = ticketService.updateTicket(ticketRequest);
        return ResponseEntity.ok(new TicketTrainerResponse(update));
    }

    // Pageable 변환용 메서드
    private <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

}
