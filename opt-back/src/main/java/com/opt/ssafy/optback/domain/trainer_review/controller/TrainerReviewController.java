package com.opt.ssafy.optback.domain.trainer_review.controller;

import com.opt.ssafy.optback.domain.trainer_review.dto.TrainerReviewRequest;
import com.opt.ssafy.optback.domain.trainer_review.dto.TrainerReviewResponse;
import com.opt.ssafy.optback.domain.trainer_review.dto.TrainerReviewSummaryResponse;
import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReview;
import com.opt.ssafy.optback.domain.trainer_review.service.TrainerReviewImageService;
import com.opt.ssafy.optback.domain.trainer_review.service.TrainerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trainer-reviews")
public class TrainerReviewController {

    private final TrainerReviewService trainerReviewService;
    private final TrainerReviewImageService trainerReviewImageService;

    // 리뷰(텍스트+이미지) 추가
    @PostMapping
    public ResponseEntity<TrainerReviewResponse> addTrainerReview(@RequestBody TrainerReviewRequest reviewRequestDto) {
        // 리뷰 텍스트 저장
        TrainerReview savedTrainerReview = trainerReviewService.saveReviewText(reviewRequestDto);
        return ResponseEntity.ok(new TrainerReviewResponse(savedTrainerReview));

    }

    // 트레이너 리뷰 조회
    @GetMapping("/{trainer_id}")
    public ResponseEntity<Page<TrainerReviewResponse>> getReviewsByTrainerId(@PathVariable("trainer_id") int trainerId,
                                                                             Pageable pageable) {
        Page<TrainerReviewResponse> trainerReviews = trainerReviewService.getReviewsByTrainerId(trainerId, pageable);
        return ResponseEntity.ok(trainerReviews);
    }

    // 작성자(=로그인 멤버) 리뷰 조회
    @GetMapping
    public ResponseEntity<Page<TrainerReviewResponse>> getReviewsByMyId(Pageable pageable) {
        Page<TrainerReviewResponse> responseDtos = trainerReviewService.getReviewsByReviewerId(pageable);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/summary")
    public ResponseEntity<TrainerReviewSummaryResponse> getReviewSummary(@RequestParam Integer trainerId) {
        return ResponseEntity.ok(trainerReviewService.getReviewSummary(trainerId));
    }

}
