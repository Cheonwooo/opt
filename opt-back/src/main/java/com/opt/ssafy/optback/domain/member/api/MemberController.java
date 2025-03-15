package com.opt.ssafy.optback.domain.member.api;

import com.opt.ssafy.optback.domain.member.application.MemberService;
import com.opt.ssafy.optback.domain.member.dto.UpdateInterestRequest;
import com.opt.ssafy.optback.domain.member.dto.UpdateIntroRequest;
import com.opt.ssafy.optback.domain.member.dto.UpdateNicknameRequest;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private static final String UPDATE_SUCCESS_MESSAGE = "수정되었습니다";

    // 소개글 수정
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/intro")
    public ResponseEntity<SuccessResponse> updateIntro(@RequestBody UpdateIntroRequest request) {
        memberService.updateIntro(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message(UPDATE_SUCCESS_MESSAGE)
                .build());
    }

    @PatchMapping("/nickname")
    public ResponseEntity<SuccessResponse> updateNickname(@RequestBody UpdateNicknameRequest request) {
        memberService.updateNickname(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message(UPDATE_SUCCESS_MESSAGE)
                .build());
    }

    @PatchMapping("/profile-image")
    public ResponseEntity<SuccessResponse> updateProfileImage(@RequestPart MultipartFile image) {
        memberService.updateProfileImage(image);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message(UPDATE_SUCCESS_MESSAGE)
                .build());
    }

    @PatchMapping("/interest")
    public ResponseEntity<SuccessResponse> updateInterests(@RequestBody UpdateInterestRequest request) {
        memberService.updateInterests(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message(UPDATE_SUCCESS_MESSAGE)
                .build());
    }

}
