package com.opt.ssafy.optback.domain.member.repository;

import com.opt.ssafy.optback.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT m.nickname FROM Member m WHERE m.id = :memberId")
    String findNicknameById(@Param("memberId") int memberId);

    Member getMemberById(int id);
}
