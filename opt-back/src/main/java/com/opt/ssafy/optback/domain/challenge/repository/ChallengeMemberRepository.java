package com.opt.ssafy.optback.domain.challenge.repository;

import com.opt.ssafy.optback.domain.challenge.entity.ChallengeMember;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeMemberRepository extends JpaRepository<ChallengeMember, Integer> {
    Optional<ChallengeMember> findByChallengeIdAndMemberId(int challengeId, int memberId);


    //ChallengeMember 엔티티 대신, 특정 컬럼(여기서는 challengeId)만 선택하도록 JPQL 쿼리를 직접 작성, 그렇지 않으면 엔티티 전체를 받아와서 Integer type만 받아올 수 없음
    @Query("SELECT cm.challengeId FROM ChallengeMember cm WHERE cm.memberId = :memberId AND cm.status = :status")
    List<Integer> findChallengeIdsByMemberIdAndStatus(@Param("memberId") int memberId, @Param("status") String status);

    // challenge_id와 member_id를 이용해 특정 챌린지 멤버 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM ChallengeMember cm WHERE cm.challengeId = :challengeId AND cm.memberId = :memberId")
    void deleteByChallengeIdAndMemberId(@Param("challengeId") int challengeId, @Param("memberId") int memberId);

    boolean existsByChallengeIdAndMemberId(int id, int id1);

    List<ChallengeMember> findByChallengeId(int id);

}
