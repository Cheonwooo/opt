package com.opt.ssafy.optback.domain.badge.service;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.badge.entity.Badge;
import com.opt.ssafy.optback.domain.badge.entity.BadgeDefinition;
import com.opt.ssafy.optback.domain.badge.entity.MemberBadge;
import com.opt.ssafy.optback.domain.badge.evaluator.BadgeEvaluator;
import com.opt.ssafy.optback.domain.badge.exception.BadgeEvaluatorException;
import com.opt.ssafy.optback.domain.badge.exception.BadgeException;
import com.opt.ssafy.optback.domain.badge.repository.BadgeDefinitionRepository;
import com.opt.ssafy.optback.domain.badge.repository.BadgeRepository;
import com.opt.ssafy.optback.domain.badge.repository.MemberBadgeRepository;
import com.opt.ssafy.optback.domain.chat.dto.SystemMessageToMember;
import com.opt.ssafy.optback.domain.chat.service.SystemMessageService;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.push.application.PushService;
import com.opt.ssafy.optback.domain.push.entity.FcmToken;
import com.opt.ssafy.optback.domain.push.repository.FcmTokenRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class BadgeService {

    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final Map<ActivityType, BadgeEvaluator> evaluators;
    private final PushService pushService;
    private final FcmTokenRepository fcmTokenRepository;
    private final SystemMessageService systemMessageService;
    private final UserDetailsServiceImpl userDetailsService;

    public BadgeService(List<BadgeEvaluator> evaluatorList, BadgeDefinitionRepository badgeDefinitionRepository,
                        MemberBadgeRepository memberBadgeRepository, BadgeRepository badgeRepository,
                        PushService pushService,
                        FcmTokenRepository fcmTokenRepository, SystemMessageService systemMessageService,
                        UserDetailsServiceImpl userDetailsService) {
        this.evaluators = evaluatorList.stream()
                .collect(Collectors.toMap(BadgeEvaluator::getType, Function.identity()));
        this.badgeDefinitionRepository = badgeDefinitionRepository;
        this.memberBadgeRepository = memberBadgeRepository;
        this.badgeRepository = badgeRepository;
        this.pushService = pushService;
        this.fcmTokenRepository = fcmTokenRepository;
        this.systemMessageService = systemMessageService;
        this.userDetailsService = userDetailsService;
    }

    public List<Badge> findAllBadges() {
        return badgeRepository.findAll();
    }

    public boolean hasBadge(Member member, int badgeId) {
        return memberBadgeRepository.existsByMemberIdAndBadgeId(member.getId(), badgeId);
    }

    @Transactional
    public void checkAndSaveBadges(Member member, ActivityType activityType) {
        List<BadgeDefinition> badgeDefinitions = badgeDefinitionRepository.findByActivityType(activityType);

        for (BadgeDefinition definition : badgeDefinitions) {
            if (hasBadge(member, definition.getId())) {
                continue;
            }

            // getType을 통해 사용할 평가기 선택
            BadgeEvaluator evaluator = evaluators.values().stream()
                    .filter(e -> e.getType() == definition.getActivityType())
                    .findFirst()
                    .orElse(null);

            if (evaluator == null) {
                throw new BadgeEvaluatorException("뱃지 평가기를 찾지 못하였습니다");
            }

            if (evaluator.evaluate(member, definition.getCondition())) {
                saveBadge(member, definition);
            }
        }
    }

    @Transactional
    public List<MemberBadge> findBadgesByMemberId() {
        Member member = userDetailsService.getMemberByContextHolder();
        return memberBadgeRepository.findMemberBadgeByMemberId(member.getId());
    }

    @Transactional
    public void saveBadge(Member member, BadgeDefinition badgeDefinition) {
        Badge badge = badgeRepository.findById(badgeDefinition.getBadgeId())
                .orElseThrow(() -> new BadgeEvaluatorException("뱃지를 찾을 수 없습니다"));

        MemberBadge memberBadge = MemberBadge.create(member, badge);
        memberBadgeRepository.save(memberBadge);

        System.out.println("✅ 업적 획득! " + member.getId() + "번 ID 멤버가" + badge.getId() + "번 업적을 획득했습니다");

        try {
            SystemMessageToMember systemMessageToMember = SystemMessageToMember.builder()
                    .receiverId(member.getId())
                    .content(badge.getName() + " 업적을 획득했습니다")
                    .senderId(0)
                    .build();
            systemMessageService.sendSystemMessageToMember(systemMessageToMember);
        } catch (Exception e) {
            throw new BadgeException("뱃지 채팅 메시지를 보내는 데 실패하였습니다");
        }
        try {
            String title = "뱃지 획득 알림";
            String body = "새로운 뱃지를 획득하였습니다";
            Map<String, String> data = Map.of("badgeId", String.valueOf(badge.getId()));
            Optional<FcmToken> tokens = fcmTokenRepository.findByMemberId((long) member.getId());
            if (tokens.isEmpty()) {
                log.info("토큰 없어서 못보냄");
                return;
            }
            String token = tokens.get().getToken();
            pushService.sendPushMessage(title, body, data, token);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadgeException("새로운 뱃지 획득 메시지를 보내는 데 실패하였습니다");
        }
    }


    @Transactional
    public void addBadge(Member member, Integer badgeId) {
        Badge badge = badgeRepository.findById(badgeId).orElse(null);
        MemberBadge memberBadge = MemberBadge.builder()
                .member(member)
                .badge(badge)
                .build();
        memberBadgeRepository.save(memberBadge);
    }

}
