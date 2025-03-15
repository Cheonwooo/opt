package com.opt.ssafy.optback.domain.onboarding.application;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.entity.Interest;
import com.opt.ssafy.optback.domain.member.entity.MemberInterest;
import com.opt.ssafy.optback.domain.member.repository.InterestRepository;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import com.opt.ssafy.optback.domain.onboarding.dto.OnboardingRequest;
import com.opt.ssafy.optback.domain.onboarding.exception.AlreadyOnboardedException;
import com.opt.ssafy.optback.domain.onboarding.exception.InterestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final InterestRepository interestRepository;

    @Transactional
    public void onboardMember(OnboardingRequest request) {
        Member member = userDetailsService.getMemberByContextHolder();

        if (member.isOnboarded()) {
            throw new AlreadyOnboardedException("이미 온보딩이 완료된 회원입니다.");
        }
        // 닉네임 업데이트
        member.updateNickname(request.getNickname());
        member.updateIsOnboarded();

        // 관심사 업데이트
        List<MemberInterest> newInterests = request.getInterestIds().stream()
                .map(interestId -> {
                    Interest interest = interestRepository.findById(interestId)
                            .orElseThrow(() -> new InterestNotFoundException("존재하지 않는 관심사 ID: " + interestId));
                    return new MemberInterest(null, member, interest); // ID는 자동 생성
                })
                .collect(Collectors.toList());

        member.updateInterests(newInterests);

        // 저장
        memberRepository.save(member);
    }
}
