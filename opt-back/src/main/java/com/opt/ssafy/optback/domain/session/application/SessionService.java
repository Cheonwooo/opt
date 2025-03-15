package com.opt.ssafy.optback.domain.session.application;

import com.opt.ssafy.optback.domain.session.dto.CreateSessionRecordRequest;
import com.opt.ssafy.optback.domain.session.dto.CreateSessionRequest;
import com.opt.ssafy.optback.domain.session.dto.SessionCheckRequest;
import com.opt.ssafy.optback.domain.session.dto.SessionRecordResponse;
import com.opt.ssafy.optback.domain.session.dto.SessionResponse;
import com.opt.ssafy.optback.domain.session.dto.UpdateSessionRecordRequest;
import com.opt.ssafy.optback.domain.session.dto.UpdateSessionRequest;
import com.opt.ssafy.optback.domain.session.entity.Session;
import com.opt.ssafy.optback.domain.session.entity.SessionRecord;
import com.opt.ssafy.optback.domain.session.exception.SessionNotFoundException;
import com.opt.ssafy.optback.domain.session.exception.SessionRecordNotFoundException;
import com.opt.ssafy.optback.domain.session.repository.SessionRecordRepository;
import com.opt.ssafy.optback.domain.session.repository.SessionRepository;
import com.opt.ssafy.optback.domain.ticket.repository.TicketRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionRecordRepository sessionRecordRepository;
    private final TicketRepository ticketRepository;
//    private final TicketRepository ticketRepository;

    // GET /sessions - 수업 일정 조회
    public List<SessionResponse> getSessions() {
        List<Session> sessions = sessionRepository.findAll();
        return sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // POST /sessions - 수업 일정 등록
    public SessionResponse createSession(CreateSessionRequest request) {

//        if (request.getTicketId() != null) {
//            if (!ticketRepository.existsById(request.getTicketId())) {
//                throw new TicketNotFoundException("Ticket not found with id: " + request.getTicketId());
//            }
//        }

        Session session = Session.builder()
                .ticket(ticketRepository.getReferenceById(request.getTicketId()))
                .number(request.getNumber())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .isMemberSigned(false)
                .isTrainerSigned(false)
                .build();
        Session saved = sessionRepository.save(session);
        return mapToResponse(saved);
    }

    // PATCH /sessions - 수업 일정 수정
    @Transactional
    public SessionResponse updateSession(UpdateSessionRequest request) {
        Session session = sessionRepository.findById(request.getId())
                .orElseThrow(() -> new SessionNotFoundException("Session not found with id: " + request.getId()));
        session.updateSession(request.getStartAt(), request.getEndAt());
        // @Transactional에 의해 변경 사항이 자동으로 커밋됨
        return mapToResponse(session);
    }

    // DELETE /sessions - 수업 일정 삭제
    public void deleteSession(int id) {
        if (!sessionRepository.existsById(id)) {
            throw new SessionNotFoundException("Session not found with id: " + id);
        }
        sessionRepository.deleteById(id);
    }

    // GET /sessions/{session_id} - 해당 수업의 운동 기록 조회
    public List<SessionRecordResponse> getSessionRecords(int id) {
        List<SessionRecord> sessionRecords = sessionRecordRepository.findBySessionId(id);
        if (sessionRecords.isEmpty()) {
            throw new SessionRecordNotFoundException("Session not found with id: " + id);
        }
        return sessionRecords.stream()
                .map(record -> mapToResponse(record, id))
                .collect(Collectors.toList());
    }

    // POST /sessions/exercise-records - 수업별 운동 기록 등록
    public SessionRecord createSessionRecord(CreateSessionRecordRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(
                        () -> new SessionNotFoundException("Session not found with id: " + request.getSessionId()));
        SessionRecord record = SessionRecord.builder()
                .session(session)
                .exerciseId(request.getExerciseId())
                .setCount(request.getSetCount())
                .repCount(request.getRepCount())
                .weight(request.getWeight())
                .duration(request.getDuration())
                .distance(request.getDistance())
                .build();
        return sessionRecordRepository.save(record);
    }

    // PATCH /sessions/exercise-records - 수업별 운동 기록 수정
    @Transactional
    public SessionRecord updateSessionRecord(UpdateSessionRecordRequest request) {
        SessionRecord record = sessionRecordRepository.findById(request.getId())
                .orElseThrow(() -> new SessionRecordNotFoundException(
                        "Session record not found with id: " + request.getId()));
        // 엔티티 내부의 도메인 메서드를 호출하여 업데이트
        record.updateFields(request.getSetCount(), request.getRepCount(), request.getWeight(), request.getDuration(),
                request.getDistance());
        return record;  // @Transactional에 의해 변경 사항이 커밋됨
    }

    // PATCH /sessions/trainercheck - PT 회차별 PT완료 체크하기
    @Transactional
    public SessionResponse trainerCheckSession(SessionCheckRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(
                        () -> new SessionNotFoundException("Session not found with id: " + request.getSessionId()));
        session.markTrainerSigned();
        return mapToResponse(session);
    }

    // PATCH /sessions/membercheck - PT 회차별 PT완료 체크하기
    @Transactional
    public SessionResponse memberCheckSession(SessionCheckRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(
                        () -> new SessionNotFoundException("Session not found with id: " + request.getSessionId()));
        session.markMemberSigned();
        return mapToResponse(session);
    }

    // Helper: 엔티티 -> DTO 매핑
    private SessionResponse mapToResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .ticketId(session.getTicket().getId())
                .number(session.getNumber())
                .startAt(session.getStartAt())
                .endAt(session.getEndAt())
                .isMemberSigned(session.isMemberSigned())
                .isTrainerSigned(session.isTrainerSigned())
                .build();
    }

    private SessionRecordResponse mapToResponse(SessionRecord record, Integer sessionId) {
        return SessionRecordResponse.builder()
                .id(record.getId())
                .sessionId(sessionId)
                .exerciseId(record.getExerciseId())
                .setCount(record.getSetCount())
                .repCount(record.getRepCount())
                .weight(record.getWeight())
                .duration(record.getDuration())
                .distance(record.getDistance())
                .build();
    }
}
