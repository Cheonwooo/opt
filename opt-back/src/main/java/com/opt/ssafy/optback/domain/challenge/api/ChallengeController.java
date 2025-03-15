package com.opt.ssafy.optback.domain.challenge.api;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.challenge.application.ChallengeService;
import com.opt.ssafy.optback.domain.challenge.dto.ChallengeRecordRequest;
import com.opt.ssafy.optback.domain.challenge.dto.ChallengeRecordResponse;
import com.opt.ssafy.optback.domain.challenge.dto.ChallengeRecordWithRankResponse;
import com.opt.ssafy.optback.domain.challenge.dto.ChallengeResponse;
import com.opt.ssafy.optback.domain.challenge.dto.ContributionResponse;
import com.opt.ssafy.optback.domain.challenge.dto.CreateChallengeRequest;
import com.opt.ssafy.optback.domain.challenge.dto.JoinChallengeRequest;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserDetailsServiceImpl userDetailsService;

    // 기존 전체 챌린지 조회
    @GetMapping
    public ResponseEntity<Page<ChallengeResponse>> getChallenges(
            @RequestParam(defaultValue = "PROGRESS", required = false) String status,
            Pageable pageable) {
        Page<ChallengeResponse> challenges = challengeService.getChallenges(status, pageable);
        return ResponseEntity.ok(challenges);
    }

    // GET /challenges/{id} - 특정 챌린지 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponse> getChallenge(@PathVariable int id) {
        return ResponseEntity.ok(challengeService.getChallengeById(id));
    }

    @GetMapping("/{id}/contributions")
    public ResponseEntity<List<ContributionResponse>> getChallengeContributions(@PathVariable int id) {
        return ResponseEntity.ok(challengeService.getChallengeContributions(id));
    }

    // POST /challenges - 챌린지 생성 (TRAINER 전용)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<SuccessResponse> createChallenge(CreateChallengeRequest createChallengeRequest) {
        challengeService.createChallenge(createChallengeRequest);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("챌린지가 성공적으로 생성되었습니다.")
                .build());
    }


    // DELETE /challenges/{id} - 챌린지 삭제 (TRAINER 전용)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<SuccessResponse> deleteChallenge(@PathVariable int id) {
        challengeService.deleteChallenge(id);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("챌린지가 성공적으로 삭제되었습니다.")
                .build());
    }

    // GET /challenges/record - 챌린지 수행 기록 조회
    @GetMapping("/record")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChallengeRecordResponse>> getChallengeRecords() {
        Member member = userDetailsService.getMemberByContextHolder();
        List<ChallengeRecordResponse> records = challengeService.getChallengeRecords(member.getId());
        return ResponseEntity.ok(records);
    }

    @GetMapping("/record/{challengeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChallengeRecordWithRankResponse> getChallengeRecord(@PathVariable int challengeId) {
        Member member = userDetailsService.getMemberByContextHolder();
        ChallengeRecordWithRankResponse record = challengeService.getChallengeRecord(member.getId(), challengeId);
        return ResponseEntity.ok(record);
    }

    // POST /challenges/record - 챌린지 수행 기록 등록
    @PostMapping("/record")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> recordChallenge(@RequestBody ChallengeRecordRequest request) {
        Member member = userDetailsService.getMemberByContextHolder();
        challengeService.recordChallenge(
                member.getId(),
                request.getChallengeId(),
                request.getCount(),
                request.getDuration(),
                request.getDistance()
        );
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("운동 기록이 저장되었습니다.")
                .build());
    }


    // POST /challenges/join - 챌린지 참여
    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> joinChallenge(@RequestBody JoinChallengeRequest request) {
        challengeService.joinChallenge(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("성공적으로 챌린지에 참여하였습니다.")
                .build());
    }

    // DELETE /challenges/quit?id={challengeId} - 챌린지 탈퇴
    @DeleteMapping("/quit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> leaveChallenge(@RequestParam("id") int challengeId) {
        challengeService.leaveChallenge(challengeId);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("성공적으로 챌린지를 탈퇴하였습니다.")
                .build());
    }


    // GET /challenges/created - 내(트레이너)가 생성한 챌린지 목록
    @GetMapping("/created")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<List<ChallengeResponse>> getCreatedChallenges() {
        return ResponseEntity.ok(challengeService.getCreatedChallenges());
    }

    // GET /challenges/participating - 내가 참여중인 챌린지 목록
    @GetMapping("/participating")
    public ResponseEntity<List<ChallengeResponse>> getParticipatingChallenges() {
        return ResponseEntity.ok(challengeService.getParticipatingChallenges());
    }

    // GET /challenges/applied - 내가 신청한 챌린지 목록
    @GetMapping("/applied")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChallengeResponse>> getAppliedChallenges() {
        return ResponseEntity.ok(challengeService.getAppliedChallenges());
    }

    // GET /challenges/past - 내가 참여했던 챌린지 목록
    @GetMapping("/past")
    public ResponseEntity<List<ChallengeResponse>> getPastChallenges() {
        return ResponseEntity.ok(challengeService.getPastChallenges());
    }

}
