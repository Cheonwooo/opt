package com.opt.ssafy.optback.domain.counsel.application;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.counsel.dto.CounselCreateRequest;
import com.opt.ssafy.optback.domain.counsel.dto.CounselResponse;
import com.opt.ssafy.optback.domain.counsel.dto.CounselUpdateRequest;
import com.opt.ssafy.optback.domain.counsel.entity.Counsel;
import com.opt.ssafy.optback.domain.counsel.exception.CounselNotFoundException;
import com.opt.ssafy.optback.domain.counsel.exception.TrainerNotFoundException;
import com.opt.ssafy.optback.domain.counsel.repository.CounselRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselService {

    private final CounselRepository counselRepository;
    // 토큰에서 로그인한 회원 정보를 가져오는 서비스
    private final UserDetailsServiceImpl userDetailsService;
    private final MemberRepository memberRepository;

    // 상담 일정 조회 (전체 목록)
    public List<CounselResponse> getCounsels() {
        List<Counsel> counsels = counselRepository.findAll();
        return counsels.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 상담 일정 등록
    public CounselResponse createCounsel(CounselCreateRequest request) {
        // 로그인한 회원 정보 가져오기 (상담 예약한 회원)
        Member member = userDetailsService.getMemberByContextHolder();
        // 전달받은 trainerId가 실제 member 테이블에 존재하는지 확인
        Member trainer = memberRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with id: " + request.getTrainerId()));

        Counsel counsel = Counsel.builder()
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .trainerId(request.getTrainerId())
                .memberId(member.getId())
                .status("REQUESTED") // 기본 상태 설정
                .build();
        Counsel saved = counselRepository.save(counsel);
        return mapToResponse(saved);
    }

    // 상담 일정 수정
    public CounselResponse updateCounsel(CounselUpdateRequest request) {
        Counsel counsel = counselRepository.findById(request.getId())
                .orElseThrow(() -> new CounselNotFoundException("Counsel not found with id: " + request.getId()));
        // 도메인 메서드를 이용하여 상담 일정 수정
        counsel.updateCounsel(request.getStartAt(), request.getEndAt(), request.getStatus());
        Counsel updated = counselRepository.save(counsel);
        return mapToResponse(updated);
    }

    // 상담 일정 삭제
    public void deleteCounsel(int id) {
        if (!counselRepository.existsById(id)) {
            throw new CounselNotFoundException("Counsel not found with id: " + id);
        }
        counselRepository.deleteById(id);
    }

    private CounselResponse mapToResponse(Counsel counsel) {
        return CounselResponse.builder()
                .id(counsel.getId())
                .startAt(counsel.getStartAt())
                .endAt(counsel.getEndAt())
                .memberId(counsel.getMemberId())
                .trainerId(counsel.getTrainerId())
                .status(counsel.getStatus())
                .build();
    }
}
