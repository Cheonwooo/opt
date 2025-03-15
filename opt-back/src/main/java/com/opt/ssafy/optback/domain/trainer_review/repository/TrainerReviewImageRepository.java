package com.opt.ssafy.optback.domain.trainer_review.repository;

import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerReviewImageRepository extends JpaRepository<TrainerReviewImage, Integer> {
}
