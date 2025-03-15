package com.opt.ssafy.optback.domain.trainer_review.dto;

import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReview;
import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReviewImage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrainerReviewResponse {

    private Integer id;
    private Integer trainerId;
    private String comment;
    private LocalDateTime createdAt;
    private Integer rate;
    private List<String> imageUrls;

    public TrainerReviewResponse(TrainerReview trainerReview) {
        this.id = trainerReview.getId();
        this.trainerId = trainerReview.getTrainerDetail().getTrainerId();
        this.comment = trainerReview.getComment();
        this.createdAt = trainerReview.getCreatedAt();
        this.rate = trainerReview.getRate();
        List<String> imageUrlList = new ArrayList<>();
        if (trainerReview.getImages() != null) {
            for (TrainerReviewImage image : trainerReview.getImages()) {
                imageUrlList.add(image.getPath());
            }
        }
        this.imageUrls = imageUrlList;
    }

}
