package com.opt.ssafy.optback.domain.badge.repository;

import com.opt.ssafy.optback.domain.badge.entity.MemberBadge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Integer> {

    boolean existsByMemberIdAndBadgeId(int memberId, int badgeId);

    List<MemberBadge> findMemberBadgeByMemberId(int memberId);

}
