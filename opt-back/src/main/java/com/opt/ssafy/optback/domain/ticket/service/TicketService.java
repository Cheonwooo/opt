package com.opt.ssafy.optback.domain.ticket.service;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.exception.MemberNotFoundException;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import com.opt.ssafy.optback.domain.session.dto.SessionResponse;
import com.opt.ssafy.optback.domain.ticket.dto.TicketRequest;
import com.opt.ssafy.optback.domain.ticket.dto.UpdateTicketRequest;
import com.opt.ssafy.optback.domain.ticket.entity.Ticket;
import com.opt.ssafy.optback.domain.ticket.exception.TicketNotFoundException;
import com.opt.ssafy.optback.domain.ticket.repository.TicketRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final MemberRepository memberRepository;

    // 티켓 생성
    @Transactional
    public Ticket saveTicket(TicketRequest request) {
        Member member = userDetailsService.getMemberByContextHolder();
        Member student = memberRepository.findByEmail(request.getStudentEmail()).orElseThrow(
                MemberNotFoundException::new);

        Ticket newTicket = Ticket.builder()
                .trainer(member)
                .student(student)
                .price(request.getPrice())
                .totalSessions(request.getTotalSessions())
                .status("대기")
                .build();

        return ticketRepository.save(newTicket);
    }

    // 티켓 수정(삭제 포함)
    @Transactional
    public Ticket updateTicket(UpdateTicketRequest request) {
        Ticket findTicket = ticketRepository.getReferenceById(request.getId());
        findTicket.setNewRecord(request);
        return ticketRepository.save(findTicket);
    }

    // 티켓 조회
    public List<Ticket> getTicketsByTrainerIdAndIsUsed() {
        Member member = userDetailsService.getMemberByContextHolder();
        return ticketRepository.findByTrainerIdAndLastUsedDateIsNotNull(member.getId());
    }

    public List<Ticket> getTicketsByStudentIdAndIsUsed() {
        Member member = userDetailsService.getMemberByContextHolder();
        return ticketRepository.findByStudentIdAndLastUsedDateIsNotNull(member.getId());
    }

    public List<Ticket> getTicketsByTrainerIdAndIsNotUsed() {
        Member member = userDetailsService.getMemberByContextHolder();
        return ticketRepository.findByTrainerIdAndLastUsedDateIsNull(member.getId());
    }

    public List<Ticket> getTicketsByStudentIdAndIsNotUsed() {
        Member member = userDetailsService.getMemberByContextHolder();
        return ticketRepository.findByStudentIdAndLastUsedDateIsNull(member.getId());
    }

    // 세션 횟수 차감 시 업데이트
    public Ticket updateUsed(SessionResponse session) {
        Ticket findTicket = ticketRepository.getReferenceById(session.getTicketId());
        if (findTicket == null) {
            throw new TicketNotFoundException("티켓을 찾지 못하였습니다");
        }

        boolean studentUsed = session.isMemberSigned();
        boolean trainerUsed = session.isTrainerSigned();

        // 멤버와 트레이너 모두 체크한 경우 세션 횟수 업데이트
        if (studentUsed && trainerUsed) {
            int update = findTicket.getUsedSessions() + 1;
            findTicket.setUsedSessions(update);

            // 세션 횟수를 모두 소진했을 경우
            if (update == findTicket.getTotalSessions()) {
                LocalDate lastUsedDate =
                        (session.getEndAt() != null) ? session.getEndAt().toLocalDate() : LocalDate.now();
                findTicket.setLastUsedDate(lastUsedDate);
            }
        }

        return ticketRepository.save(findTicket);
    }

}
