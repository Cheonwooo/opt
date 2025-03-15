package com.opt.ssafy.optback.domain.trainer_review.service;

import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReview;
import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReviewImage;
import com.opt.ssafy.optback.domain.trainer_review.repository.TrainerReviewImageRepository;
import com.opt.ssafy.optback.global.application.S3Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerReviewImageService {

    private final TrainerReviewImageRepository trainerReviewImageRepository;
    private final S3Service s3Service;
    private final UserDetailsService userDetailsService;

    @Value("${review.image.bucket.name}")
    private String bucket;

    // 리뷰 이미지 저장
    @Transactional
    public List<String> saveReviewImages(TrainerReview trainerReview, List<MultipartFile> images) {
        List<TrainerReviewImage> savedImages = new ArrayList<>();
        for (MultipartFile image : images) {
            TrainerReviewImage trainerReviewImage = uploadImageToS3(image, trainerReview);
            savedImages.add(trainerReviewImage);
            // 엔티티에 추가 (양방향 연관관계라면 필요)
            trainerReview.addImage(trainerReviewImage);
        }
        trainerReviewImageRepository.saveAll(savedImages);
        return savedImages.stream()
                .map(TrainerReviewImage::getPath)
                .collect(Collectors.toList());
    }

    private TrainerReviewImage uploadImageToS3(MultipartFile image, TrainerReview trainerReview) {
        try {
            String path = s3Service.uploadImageFile(image, bucket);
            return TrainerReviewImage.builder()
                    .path(path)
                    .trainerReview(trainerReview)
                    .build();
        } catch (java.io.IOException e) {
            throw new RuntimeException("리뷰 이미지 업로드 실패", e);
        }
    }
}
