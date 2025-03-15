package com.opt.ssafy.optback.domain.push.repository;

import com.opt.ssafy.optback.domain.push.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {
    Optional<FcmToken> findByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    boolean existsByMemberIdAndToken(Integer id, String token);
}