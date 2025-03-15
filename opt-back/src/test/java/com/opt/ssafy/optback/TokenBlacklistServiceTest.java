package com.opt.ssafy.optback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opt.ssafy.optback.domain.auth.application.TokenBlacklistService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class TokenBlacklistServiceTest {
    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 블랙리스트_토큰_저장() {
        // given
        String token = "sampleToken";
        long expirationTime = 60000L;

        // when
        tokenBlacklistService.blackList(token, expirationTime);

        // then
        verify(valueOperations, times(1))
                .set(eq(token), eq("blacklisted"), eq(expirationTime), eq(TimeUnit.MICROSECONDS));
    }

    @Test
    void 블랙리스트에_저장된_토큰일때_true() {
        // given
        String token = "sampleToken";
        when(redisTemplate.hasKey(token)).thenReturn(true);

        // when
        boolean result = tokenBlacklistService.isBlackListed(token);

        // then
        assertThat(result).isTrue();
    }

}
