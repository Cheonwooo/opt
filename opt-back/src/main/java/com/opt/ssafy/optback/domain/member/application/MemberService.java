package com.opt.ssafy.optback.domain.member.application;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.counsel.exception.TrainerNotFoundException;
import com.opt.ssafy.optback.domain.member.dto.UpdateInterestRequest;
import com.opt.ssafy.optback.domain.member.dto.UpdateIntroRequest;
import com.opt.ssafy.optback.domain.member.dto.UpdateNicknameRequest;
import com.opt.ssafy.optback.domain.member.entity.Interest;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.entity.MemberInterest;
import com.opt.ssafy.optback.domain.member.exception.DuplicatedNicknameException;
import com.opt.ssafy.optback.domain.member.repository.InterestRepository;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import com.opt.ssafy.optback.domain.member.repository.TrainerSpecialtyRepository;
import com.opt.ssafy.optback.domain.trainer_detail.Repository.TrainerDetailRepository;
import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import com.opt.ssafy.optback.domain.member.entity.TrainerSpecialty;
import com.opt.ssafy.optback.global.application.S3Service;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final UserDetailsServiceImpl userDetailsService;
    private final MemberRepository memberRepository;
    private final InterestRepository interestRepository;
    private final S3Service s3Service;
    private final TrainerDetailRepository trainerDetailRepository;
    private final KeywordExtractionService keywordExtractionService;
    private final TrainerSpecialtyRepository trainerSpecialtyRepository;

    @Value("${profile.image.bucket.name}")
    private String profileImageBucketName;

    public void updateIntro(UpdateIntroRequest request) {
        Member member = userDetailsService.getMemberByContextHolder();
        TrainerDetail trainerDetail = member.getTrainerDetail();

        if(request.getText()==null){
            throw new IllegalStateException("text가 NULL입니다.");
        }
        trainerSpecialtyRepository.deleteByTrainerId(member.getId());
        trainerDetail.updateIntro(request.getText());

        saveTrainerSpecialties();
    }

    public void updateNickname(UpdateNicknameRequest request) {
        Member member = userDetailsService.getMemberByContextHolder();
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new DuplicatedNicknameException();
        }
        member.updateNickname(request.getNickname());
    }

    public void updateProfileImage(MultipartFile image) {
        try {
            String imagePath = s3Service.uploadImageFile(image, profileImageBucketName);
            Member member = userDetailsService.getMemberByContextHolder();
            member.updateProfileImage(imagePath);
        } catch (IOException e) {
            System.err.println("업로드 실패 : " + e.getMessage());
        }
    }

    public void updateInterests(UpdateInterestRequest request) {
        Member member = userDetailsService.getMemberByContextHolder();
        List<Interest> interests = interestRepository.findAllById(request.getInterestIds());

        List<MemberInterest> newInterests = interests.stream().map(interest ->
                MemberInterest.builder()
                        .member(member)
                        .interest(interest)
                        .build()).toList();
        member.updateInterests(newInterests);
    }

    @Transactional
    public void saveTrainerSpecialties() {
        Member member = userDetailsService.getMemberByContextHolder();
        int memberId = 0;
        if(member != null) {
            memberId = member.getId();
        }
        TrainerDetail trainerDetail = trainerDetailRepository.findById(memberId)
                .orElseThrow(() -> new TrainerNotFoundException("해당 트레이너 정보를 찾을 수 없습니다: " + member.getId()));

        String intro = trainerDetail.getIntro();

        // 키워드 추출
        List<String> keywords = keywordExtractionService.extractKeywords(intro);

        for (String keyword : keywords) {
            TrainerSpecialty trainerSpecialty = new TrainerSpecialty(memberId, keyword);
            trainerSpecialtyRepository.save(trainerSpecialty);
        }
    }

}
