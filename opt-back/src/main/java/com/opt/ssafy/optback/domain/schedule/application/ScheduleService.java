package com.opt.ssafy.optback.domain.schedule.application;

import com.opt.ssafy.optback.domain.schedule.dto.ScheduleResponse;
import com.opt.ssafy.optback.domain.counsel.entity.Counsel;
import com.opt.ssafy.optback.domain.schedule.exception.ScheduleNotFoundException;
import com.opt.ssafy.optback.domain.session.entity.Session;
import com.opt.ssafy.optback.domain.counsel.repository.CounselRepository;
import com.opt.ssafy.optback.domain.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final CounselRepository counselRepository;
    private final SessionRepository sessionRepository;

    public List<ScheduleResponse> getSchedules() {
        List<Counsel> counsels = counselRepository.findAll();
        List<Session> sessions = sessionRepository.findAll();

        List<ScheduleResponse> counselResponses = counsels.stream()
                .map(counsel -> ScheduleResponse.builder()
                        .id(counsel.getId())
                        .type("COUNSEL")
                        .memberId(counsel.getMemberId())
                        .trainerId(counsel.getTrainerId())
                        .startAt(counsel.getStartAt())
                        .endAt(counsel.getEndAt())
                        .status(counsel.getStatus())
                        .build())
                .collect(Collectors.toList());

        List<ScheduleResponse> sessionResponses = sessions.stream()
                .map(session -> ScheduleResponse.builder()
                        .id(session.getId())
                        .type("SESSION")
                        .ticketId(session.getTicket().getId())
                        .number(session.getNumber())
                        .startAt(session.getStartAt())
                        .endAt(session.getEndAt())
                        .isMemberSigned(session.isMemberSigned())
                        .isTrainerSigned(session.isTrainerSigned())
                        .build())
                .toList();

        counselResponses.addAll(sessionResponses);

        if (counselResponses.isEmpty()) {
            throw new ScheduleNotFoundException("등록된 일정이 없습니다.");
        }
        return counselResponses;
    }
}
