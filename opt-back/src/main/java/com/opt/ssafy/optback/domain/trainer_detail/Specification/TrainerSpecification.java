package com.opt.ssafy.optback.domain.trainer_detail.Specification;

import com.opt.ssafy.optback.domain.gym.entity.Gym;
import com.opt.ssafy.optback.domain.member.entity.MemberInterest;
import com.opt.ssafy.optback.domain.trainer_detail.dto.TrainerSearchRequest;
import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class TrainerSpecification {

    public static Specification<TrainerDetail> filterByCriteria(TrainerSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 트레이너 이름 검색
            if (request.getName() != null && !request.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("member").get("name"), "%" + request.getName() + "%"));
            }

            // 주소 검색
            if (request.getAddress() != null && !request.getAddress().isEmpty()) {
                Join<TrainerDetail, Gym> gymJoin = root.join("gym", JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(gymJoin.get("fullAddress"), "%" + request.getAddress() + "%"),
                        criteriaBuilder.like(gymJoin.get("roadAddress"), "%" + request.getAddress() + "%")
                ));
            }

            // 관심사 필터링
            if (request.getInterests() != null && !request.getInterests().isEmpty()) {
                Join<TrainerDetail, MemberInterest> interestJoin = root.join("member")
                        .join("memberInterests", JoinType.LEFT);
                predicates.add(interestJoin.get("interest").get("displayName").in(request.getInterests()));
            }

            // 원데이 클래스
            if (request.getIsOneDayAvailable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isOneDayAvailable"), request.getIsOneDayAvailable()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
