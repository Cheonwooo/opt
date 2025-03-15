package com.opt.ssafy.optback.domain.counsel.api;

import com.opt.ssafy.optback.domain.counsel.application.CounselService;
import com.opt.ssafy.optback.domain.counsel.dto.CounselCreateRequest;
import com.opt.ssafy.optback.domain.counsel.dto.CounselResponse;
import com.opt.ssafy.optback.domain.counsel.dto.CounselUpdateRequest;
import com.opt.ssafy.optback.global.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/counsels")
public class CounselController {

    private final CounselService counselService;

    // GET /counsels - 상담 일정 조회
    @GetMapping
    public ResponseEntity<List<CounselResponse>> getCounsels() {
        List<CounselResponse> counsels = counselService.getCounsels();
        return ResponseEntity.ok(counsels);
    }

    // POST /counsels - 상담 일정 등록
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> createCounsel(@RequestBody CounselCreateRequest request) {
        counselService.createCounsel(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Counsel schedule created successfully")
                .build());
    }

    // PATCH /counsels - 상담 일정 수정
    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> updateCounsel(@RequestBody CounselUpdateRequest request) {
        counselService.updateCounsel(request);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Counsel schedule updated successfully")
                .build());
    }

    // DELETE /counsels - 상담 일정 삭제
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> deleteCounsel(@RequestParam int id) {
        counselService.deleteCounsel(id);
        return ResponseEntity.ok(SuccessResponse.builder()
                .message("Counsel schedule deleted successfully")
                .build());
    }
}
