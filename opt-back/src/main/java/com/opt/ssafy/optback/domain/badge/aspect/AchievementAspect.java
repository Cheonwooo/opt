package com.opt.ssafy.optback.domain.badge.aspect;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.badge.event.ActivityEvent;
import com.opt.ssafy.optback.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AchievementAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final UserDetailsServiceImpl userDetailsService;

    @AfterReturning("execution(* com.opt.ssafy.optback.domain.exercise.api.ExerciseRecordController.createExerciseRecord(..))")
    public void afterExerciseRecordCreation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Member member = userDetailsService.getMemberByContextHolder();

        // 설정한 ActivityType에 관한 뱃지만 탐색
        eventPublisher.publishEvent(new ActivityEvent(member, ActivityType.ATTENDANCE));
        eventPublisher.publishEvent(new ActivityEvent(member, ActivityType.EXERCISE));
    }

    @AfterReturning("execution(* com.opt.ssafy.optback.domain.challenge.application.ChallengeService.recordCount(..))")
    public void afterChallengeRecordUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Member member = userDetailsService.getMemberByContextHolder();
        
        eventPublisher.publishEvent(new ActivityEvent(member, ActivityType.CHALLENGE));
    }


}
