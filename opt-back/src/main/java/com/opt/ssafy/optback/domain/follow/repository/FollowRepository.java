package com.opt.ssafy.optback.domain.follow.repository;

import com.opt.ssafy.optback.domain.follow.entity.Follow;
import com.opt.ssafy.optback.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    @Query("SELECT DISTINCT f FROM Follow f WHERE f.member.id = :memberId")
    List<Follow> findByMember(@Param("memberId") int memberId);

    // ✅ 나를 팔로우한 사람들 조회 (팔로워)
    @Query("SELECT DISTINCT f FROM Follow f WHERE f.target.id = :targetId")
    List<Follow> findByTarget(@Param("targetId") int targetId);

    // ✅ 특정 팔로우 관계 삭제
    void deleteByMemberAndTarget(Member member, Member target);

    boolean existsByMemberAndTarget(Member member, Member target);
}