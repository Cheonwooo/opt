package com.opt.ssafy.optback.domain.trainer_detail.Service;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.counsel.exception.TrainerNotFoundException;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.entity.MemberInterest;
import com.opt.ssafy.optback.domain.member.entity.TrainerSpecialty;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import com.opt.ssafy.optback.domain.member.repository.TrainerSpecialtyRepository;
import com.opt.ssafy.optback.domain.menu.dto.MenuResponse;
import com.opt.ssafy.optback.domain.menu.repository.MenuRepository;
import com.opt.ssafy.optback.domain.trainer_detail.Repository.TrainerDetailRepository;
import com.opt.ssafy.optback.domain.trainer_detail.Specification.TrainerSpecification;
import com.opt.ssafy.optback.domain.trainer_detail.dto.TrainerDetailResponse;
import com.opt.ssafy.optback.domain.trainer_detail.dto.TrainerSearchRequest;
import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import com.opt.ssafy.optback.domain.trainer_review.repository.TrainerReviewRepository;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerDetailService {

    private final TrainerDetailRepository trainerDetailRepository;
    private final MemberRepository memberRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final TrainerReviewRepository trainerReviewRepository;
    private final TrainerSpecialtyRepository trainerSpecialtyRepository;
    private final MenuRepository menuRepository;


    public List<String> getTrainerSpecialties(int trainerId) {
        List<TrainerSpecialty> specialties = trainerSpecialtyRepository.findByTrainerId(trainerId);
        return specialties.stream().map(TrainerSpecialty::getKeyword).collect(Collectors.toList());
    }
    public TrainerDetailResponse getTrainerDetail(int trainerId) {
        TrainerDetail trainerDetail = trainerDetailRepository.findById(trainerId)
                .orElseThrow(() -> new TrainerNotFoundException("해당 트레이너 정보를 찾을 수 없습니다: " + trainerId));

        return getResponse(trainerDetail);
    }

    // 트레이너 검색 + 정렬
    public List<TrainerDetailResponse> searchAndSortTrainers(TrainerSearchRequest request) {
        Integer memberId = checkLogged();

        // 검색
        Specification<TrainerDetail> specification = TrainerSpecification.filterByCriteria(request);
        List<TrainerDetail> filteredTrainers = trainerDetailRepository.findAll(specification);

        // 정렬
        String sortBy = (request.getSortBy() != null) ? request.getSortBy() : "recommendation";
        double myLatitude = (request.getMyLatitude() != null) ? request.getMyLatitude().doubleValue() : 0.0;
        double myLongitude = (request.getMyLongitude() != null) ? request.getMyLongitude().doubleValue() : 0.0;

        sortTrainers(filteredTrainers, sortBy, myLatitude, myLongitude, memberId);

        return getResponses(filteredTrainers);
    }

    private Integer checkLogged() {
        Member member = userDetailsService.getMemberByContextHolder();
        return (member != null) ? member.getId() : null;
    }

    // 정렬
    private void sortTrainers(List<TrainerDetail> trainers, String sortBy, Double myLatitude, Double myLongitude,
                              Integer memberId) {
        if (trainers == null || trainers.isEmpty()) {
            return;
        }
        switch (sortBy) {
            case "recommendation":
                trainers.sort((t1, t2) -> compareByRecommendation(t1, t2, myLatitude, myLongitude, memberId));
                break;
            case "distance":
                trainers.sort((t1, t2) -> compareByDistance(t1, t2, myLatitude, myLongitude));
                break;
            case "rating":
                trainers.sort(this::compareByRating);
                break;
            case "review":
                trainers.sort(this::compareByReviewCount);
                break;
        }
    }

    // 추천순
    private int compareByRecommendation(TrainerDetail t1, TrainerDetail t2, Double userLatitude, Double userLongitude,
                                        Integer memberId) {
        // 로그인한 경우 관심사 활용
//        if (memberId != null) {
//            int matchCount1 = countInterestMatches(memberId, t1);
//            int matchCount2 = countInterestMatches(memberId, t2);
//            if (matchCount1 != matchCount2) {
//                return Integer.compare(matchCount2, matchCount1);
//            }
//        }

        // 높은 점수가 먼저 오도록 정렬
        if (memberId == null) {
            double score1 = calculateTrainerScore(t1, memberId, userLatitude, userLongitude);
            double score2 = calculateTrainerScore(t2, memberId, userLatitude, userLongitude);
            return Double.compare(score2, score1);
        }
        return compareByDistance(t1, t2, userLatitude, userLongitude);
    }

    // 관심사 매칭 개수
    private int countInterestMatches(Integer memberId, TrainerDetail trainer) {
        if (memberId == null || trainer == null || trainer.getMember() == null) {
            return 0;
        }

        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null || member.getMemberInterests() == null || member.getMemberInterests().isEmpty()) {
            return 0;
        }

        Set<String> userInterests = member.getMemberInterests().stream()
                .map(MemberInterest::getInterest)
                .filter(interest -> interest != null)
                .map(interest -> interest.getDisplayName())
                .collect(Collectors.toSet());

        if (trainer.getMember().getMemberInterests() == null || trainer.getMember().getMemberInterests().isEmpty()) {
            return 0;
        }

        Set<String> trainerInterests = trainer.getMember().getMemberInterests().stream()
                .map(MemberInterest::getInterest)
                .filter(interest -> interest != null)
                .map(interest -> interest.getDisplayName())
                .collect(Collectors.toSet());

        return (int) userInterests.stream().filter(trainerInterests::contains).count();
    }

    // 트레이너 추천 점수 계산
    private double calculateTrainerScore(TrainerDetail trainer, Integer memberId, double userLatitude,
                                         double userLongitude) {
        double score = 0.0;

        // 관심사 매칭 점수
        int interestMatches = countInterestMatches(memberId, trainer);
        score += (interestMatches * 2);

        // 평점 + 리뷰 수 반영 (평균 평점 조정)
        double averageRating = trainerReviewRepository.findAverageRatingByTrainerId(trainer.getTrainerId());
        int reviewCount = trainerReviewRepository.countReviewsByTrainerId(trainer.getTrainerId());
        double weightedRating =
                ((averageRating * reviewCount) + (4.0 * 10)) / (reviewCount + 10); // Bayesian Average 방식
        score += (weightedRating * 5);  // 평점 가중치 적용

        // 거리 가중치 반영(가까울수록 높은 점수)
        if (userLatitude != 0 && userLongitude != 0 && trainer.getGym() != null) {
            double distance = calculateDistance(userLatitude, userLongitude, trainer.getGym().getLatitude(),
                    trainer.getGym().getLongitude());
            double distanceWeight = calculateDistanceWeight(distance);  // 거리 가중치 계산
            score += distanceWeight;  // 거리 점수 추가
        }

        // 하루 이용 가능 여부 가산점
        if (trainer.getIsOneDayAvailable()) {
            score += 2;
        }

        return score;
    }

    // 거리 반비례 함수 적용
    private double calculateDistanceWeight(double distance) {
        return 10 / (1 + distance);
    }

    // 거리순
    private int compareByDistance(TrainerDetail t1, TrainerDetail t2, Double myLatitude, Double myLongitude) {
        if (t1 == null || t2 == null || t1.getGym() == null || t2.getGym() == null || myLatitude == 0
                || myLongitude == 0) {
            return 0;
        }

        double distance1 = calculateDistance(myLatitude, myLongitude, t1.getGym().getLatitude(),
                t1.getGym().getLongitude());
        double distance2 = calculateDistance(myLatitude, myLongitude, t2.getGym().getLatitude(),
                t2.getGym().getLongitude());
        return Double.compare(distance1, distance2);
    }

    // 거리 계산
    private double calculateDistance(Double lat1, Double lon1, BigDecimal lat2, BigDecimal lon2) {
        return Point2D.distance(lon1, lat1, lon2.doubleValue(), lat2.doubleValue());
    }

    // 평점순
    private int compareByRating(TrainerDetail t1, TrainerDetail t2) {
        double rating1 = trainerReviewRepository.findAverageRatingByTrainerId(t1.getTrainerId());
        double rating2 = trainerReviewRepository.findAverageRatingByTrainerId(t2.getTrainerId());
        return Double.compare(rating2, rating1);
    }

    // 리뷰순
    private int compareByReviewCount(TrainerDetail t1, TrainerDetail t2) {
        int reviewCount1 = trainerReviewRepository.countReviewsByTrainerId(t1.getTrainerId());
        int reviewCount2 = trainerReviewRepository.countReviewsByTrainerId(t2.getTrainerId());
        return Integer.compare(reviewCount2, reviewCount1);
    }

    // 추천
    public List<TrainerDetailResponse> getRecommendedTrainers(TrainerSearchRequest request) {
        Integer memberId = checkLogged();
        List<TrainerDetail> trainers = trainerDetailRepository.findAll();
        if (trainers == null || trainers.isEmpty()) {
            return List.of();
        }

        double myLatitude = (request.getMyLatitude() != null) ? request.getMyLatitude().doubleValue() : 0.0;
        double myLongitude = (request.getMyLongitude() != null) ? request.getMyLongitude().doubleValue() : 0.0;

        trainers.sort((t1, t2) -> compareByRecommendation(t1, t2, myLatitude, myLongitude, memberId));

        return getResponses(trainers);
    }

    public List<TrainerDetailResponse> getResponses(List<TrainerDetail> trainers) {
        double averageRating = trainerReviewRepository.findAverageRatingByTrainerId(trainers.get(0).getTrainerId());
        Integer reviewCount = trainerReviewRepository.countReviewsByTrainerId(trainers.get(0).getTrainerId());

        List<TrainerDetailResponse> responses = trainers.stream()
                .map(trainer -> new TrainerDetailResponse(trainer,
                        trainerSpecialtyRepository.findKeywordsByTrainerId(trainer.getTrainerId()),
                        averageRating,
                        reviewCount,
                        menuRepository.findByTrainerId(trainer.getTrainerId()).stream()
                                .map(MenuResponse::new)
                                .toList()
                ))
                .toList();

        return responses;
    }

    public TrainerDetailResponse getResponse(TrainerDetail trainer) {
        double averageRating = trainerReviewRepository.findAverageRatingByTrainerId(trainer.getTrainerId());
        Integer reviewCount = trainerReviewRepository.countReviewsByTrainerId(trainer.getTrainerId());

        return new TrainerDetailResponse(
                trainer,
                trainerSpecialtyRepository.findKeywordsByTrainerId(trainer.getTrainerId()),
                averageRating,
                reviewCount,
                menuRepository.findByTrainerId(trainer.getTrainerId()).stream()
                        .map(MenuResponse::new)
                        .toList()
        );
    }
}
