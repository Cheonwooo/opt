package com.opt.ssafy.optback.domain.session.api;

import com.opt.ssafy.optback.domain.session.application.SessionService;
import com.opt.ssafy.optback.domain.session.dto.*;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    // GET /sessions - 수업 일정 조회
    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessions() {
        return ResponseEntity.ok(sessionService.getSessions());
    }

    // POST /sessions - 수업 일정 등록
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        return ResponseEntity.ok(sessionService.createSession(request));
    }

    // PATCH /sessions - 수업 일정 수정
    @PatchMapping
    public ResponseEntity<SessionResponse> updateSession(@RequestBody UpdateSessionRequest request) {
        return ResponseEntity.ok(sessionService.updateSession(request));
    }

    // DELETE /sessions - 수업 일정 삭제 (ID는 쿼리 파라미터로 전달)
    @DeleteMapping
    public ResponseEntity<SuccessResponse> deleteSession(@RequestParam int id) {
        sessionService.deleteSession(id);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Session deleted successfully")
                .build());
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<SessionRecordResponse>> getSessions(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(sessionService.getSessionRecords(sessionId));
    }

    // POST /sessions/exercise-records - 수업별 운동 기록 등록
    @PostMapping("/exercise-records")
    public ResponseEntity<SuccessResponse> createSessionRecord(@RequestBody CreateSessionRecordRequest request) {
        sessionService.createSessionRecord(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Session record created successfully")
                .build());
    }

    // PATCH /sessions/exercise-records - 수업별 운동 기록 수정
    @PatchMapping("/exercise-records")
    public ResponseEntity<SuccessResponse> updateSessionRecord(@RequestBody UpdateSessionRecordRequest request) {
        sessionService.updateSessionRecord(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Session record updated successfully")
                .build());
    }

    // PATCH /sessions/trainercheck - PT 회차별 PT완료 체크하기
    @PatchMapping("/trainercheck")
    public ResponseEntity<SessionResponse> trainerCheckSession(@RequestBody SessionCheckRequest request) {
        return ResponseEntity.ok(sessionService.trainerCheckSession(request));
    }

    // PATCH /sessions/membercheck - PT 회차별 PT완료 체크하기
    @PatchMapping("/membercheck")
    public ResponseEntity<SessionResponse> memberCheckSession(@RequestBody SessionCheckRequest request) {
        return ResponseEntity.ok(sessionService.memberCheckSession(request));
    }
}
