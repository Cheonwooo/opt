package com.opt.ssafy.optback.domain.trainer_review.service;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.trainer_detail.Repository.TrainerDetailRepository;
import com.opt.ssafy.optback.domain.trainer_detail.Service.TrainerDetailService;
import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import com.opt.ssafy.optback.domain.trainer_review.dto.TrainerReviewRequest;
import com.opt.ssafy.optback.domain.trainer_review.dto.TrainerReviewResponse;
import com.opt.ssafy.optback.domain.trainer_review.dto.TrainerReviewSummaryResponse;
import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReview;
import com.opt.ssafy.optback.domain.trainer_review.exception.TrainerReviewNotFoundException;
import com.opt.ssafy.optback.domain.trainer_review.exception.TrainerReviewNotSaveException;
import com.opt.ssafy.optback.domain.trainer_review.repository.TrainerReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerReviewService {

    private final TrainerReviewRepository trainerReviewRepository;
    private final TrainerReviewImageService trainerReviewImageService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TrainerDetailRepository trainerDetailRepository;
    private final TrainerDetailService trainerDetailService;

    // 리뷰 저장
    @Transactional
    public TrainerReview saveReviewWithImages(TrainerReviewRequest reviewRequestDto, List<MultipartFile> images) {
        // 로그인 멤버
        Member member = userDetailsService.getMemberByContextHolder();
        int reviewerId = member.getId();

        // 리뷰 텍스트 저장
        TrainerReview savedReview = saveReviewText(reviewRequestDto);

        // 리뷰 이미지 저장
        if (images != null && !images.isEmpty()) {
            trainerReviewImageService.saveReviewImages(savedReview, images);
        }

        return savedReview;
    }

    // 리뷰 텍스트 저장
    @Transactional
    public TrainerReview saveReviewText(TrainerReviewRequest trainerReviewRequestDto) {
        // 로그인 멤버
        Member member = userDetailsService.getMemberByContextHolder();
        int reviewerId = member.getId();

        TrainerDetail trainerDetail = trainerDetailRepository.findById(trainerReviewRequestDto.getTrainerId())
                .orElseThrow(() -> new TrainerReviewNotSaveException("존재하지 않는 트레이너입니다."));

        TrainerReview newTrainerReview = TrainerReview.builder()
                .reviewerId(reviewerId)
                .trainerDetail(trainerDetail)
                .comment(trainerReviewRequestDto.getComment())
                .rate(trainerReviewRequestDto.getRate())
                .build();

        // 유효성 검사
        if (newTrainerReview.getReviewerId() == 0) {
            throw new TrainerReviewNotSaveException("리뷰어 ID가 없습니다");
        }
        if (newTrainerReview.getTrainerDetail() == null) {
            throw new TrainerReviewNotSaveException("트레이너 ID가 없습니다");
        }
        if (newTrainerReview.getComment() == null || newTrainerReview.getComment().trim().isEmpty()) {
            throw new TrainerReviewNotSaveException("리뷰 내용이 없습니다");
        }
        if (newTrainerReview.getRate() < 1 || newTrainerReview.getRate() > 5) {
            throw new TrainerReviewNotSaveException("평점 값이 유효하지 않습니다");
        }

        TrainerReview savedTrainerReview = trainerReviewRepository.save(newTrainerReview);

        return savedTrainerReview;
    }

    // 트레이너 ID로 리뷰 조회
    public Page<TrainerReviewResponse> getReviewsByTrainerId(int trainerId, Pageable pageable) {
        TrainerDetail trainerDetail = trainerDetailRepository.findById(trainerId)
                .orElseThrow(() -> new TrainerReviewNotFoundException("트레이너 ID 값이 잘못되었습니다"));
        List<TrainerReviewResponse> reviews = trainerReviewRepository.findByTrainerDetailAndIsDeletedIsFalse(
                        trainerDetail, pageable)
                .stream()
                .map(TrainerReviewResponse::new)
                .toList();
        return new PageImpl<>(reviews, pageable, reviews.size());
    }

    // 멤버 ID(=로그인 멤버)로 리뷰 조회
    public Page<TrainerReviewResponse> getReviewsByReviewerId(Pageable pageable) {

        Member member = userDetailsService.getMemberByContextHolder();
        int reviewerId = member.getId();
        List<TrainerReviewResponse> myReviews = trainerReviewRepository.findByreviewerId(reviewerId, pageable)
                .stream()
                .map(TrainerReviewResponse::new)
                .toList();
        return new PageImpl<>(myReviews, pageable, myReviews.size());
    }

    public TrainerReviewSummaryResponse getReviewSummary(Integer trainerId) {
        Integer count = trainerReviewRepository.countReviewsByTrainerId(trainerId);
        Double averageRate = trainerReviewRepository.findAverageRatingByTrainerId(trainerId);
        return TrainerReviewSummaryResponse.builder()
                .count(count)
                .averageRate(averageRate)
                .build();
    }

}
