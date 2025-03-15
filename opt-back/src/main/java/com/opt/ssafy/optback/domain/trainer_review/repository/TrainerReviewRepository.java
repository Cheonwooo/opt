package com.opt.ssafy.optback.domain.trainer_review.repository;

import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import com.opt.ssafy.optback.domain.trainer_review.entity.TrainerReview;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerReviewRepository extends JpaRepository<TrainerReview, Integer> {

    List<TrainerReview> findByreviewerId(Integer reviewerId, Pageable pageable);

    List<TrainerReview> findByTrainerDetailAndIsDeletedIsFalse(TrainerDetail trainerDetail, Pageable pageable);

    // 트레이너 평균 별점
    @Query("SELECT COALESCE(AVG(tr.rate), 0) FROM TrainerReview tr WHERE tr.trainerDetail.trainerId = :trainerId")
    Double findAverageRatingByTrainerId(@Param("trainerId") Integer trainerId);

    // 트레이너의 리뷰 개수
    @Query("SELECT COUNT(tr) FROM TrainerReview tr WHERE tr.trainerDetail.trainerId = :trainerId")
    Integer countReviewsByTrainerId(@Param("trainerId") Integer trainerId);
}
