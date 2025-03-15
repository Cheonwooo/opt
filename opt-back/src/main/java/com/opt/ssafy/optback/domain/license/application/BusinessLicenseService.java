package com.opt.ssafy.optback.domain.license.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.gym.entity.Gym;
import com.opt.ssafy.optback.domain.gym.repository.GymRepository;
import com.opt.ssafy.optback.domain.license.infrastructure.BusinessLicenseProducer;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.exception.MemberNotFoundException;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import com.opt.ssafy.optback.global.application.S3Service;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessLicenseService {

    @Value("${business.license.bucket.name}")
    private String bucketName;
    private final UserDetailsServiceImpl userDetailsService;
    private final S3Service s3Service;
    private final BusinessLicenseProducer businessLicenseProducer;
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;

    @Transactional
    public void registerBusinessLicense(MultipartFile image) throws IOException {
        String path = s3Service.uploadImageFile(image, bucketName);
        Integer memberId = userDetailsService.getMemberByContextHolder().getId();
        businessLicenseProducer.sendBusinessLicenseMessage(path, memberId);
    }

    @Transactional
    public void handleBusinessLicenseResponse(JsonNode jsonNode) {
        JsonNode gymIdNode = jsonNode.get("gym_id");
        String resultMessage = jsonNode.get("message").toString();
        if (gymIdNode == null || gymIdNode.isNull()) {
            // TODO 실패 메시지 보내기
            // sendResultMessage(resultMessage);
            return;
        }
        log.info("맞는 헬스장 데이터 : {}", jsonNode.toPrettyString());
        Integer memberId = Integer.parseInt(jsonNode.get("user_id").toString());
        Integer gymId = Integer.parseInt(jsonNode.get("gym_id").toString());

        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Gym gym = gymRepository.findById(gymId).orElse(null);

        TrainerDetail trainerDetail = TrainerDetail.builder()
                .trainerId(member.getId())
                .gym(gym)
                .build();

        member.grantTrainerRole(trainerDetail);
    }

}
