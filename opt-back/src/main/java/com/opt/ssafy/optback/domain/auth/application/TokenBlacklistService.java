package com.opt.ssafy.optback.domain.auth.application;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    // 로그아웃한 유저의 토큰을 블랙리스트로 저장
    public void blackList(String token, long expirationTime) {
        stringRedisTemplate.opsForValue().set(token, "blacklisted", expirationTime, TimeUnit.MICROSECONDS);
    }

    // 토큰이 블랙리스트에 있는지 확인
    public boolean isBlackListed(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(token));
    }


}
