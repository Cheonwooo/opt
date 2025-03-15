package com.opt.ssafy.optback.domain.badge.repository;

import com.opt.ssafy.optback.domain.badge.dto.ActivityType;
import com.opt.ssafy.optback.domain.badge.entity.BadgeDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BadgeDefinitionRepository {

    private final Map<ActivityType, List<BadgeDefinition>> badgeDefinitionMap = new HashMap<>();

    public BadgeDefinitionRepository() {
        addBadgeDefinition(ActivityType.CHALLENGE,
                new BadgeDefinition(1, 1, ActivityType.CHALLENGE, Map.of("challengeClear", 1)));
        addBadgeDefinition(ActivityType.CHALLENGE,
                new BadgeDefinition(2, 2, ActivityType.CHALLENGE, Map.of("challengeClear", 5)));
        addBadgeDefinition(ActivityType.CHALLENGE,
                new BadgeDefinition(3, 3, ActivityType.CHALLENGE, Map.of("challengeClear", 10)));
        addBadgeDefinition(ActivityType.CHALLENGE,
                new BadgeDefinition(4, 4, ActivityType.CHALLENGE, Map.of("challengeClear", 20)));

        addBadgeDefinition(ActivityType.EXERCISE,
                new BadgeDefinition(5, 5, ActivityType.EXERCISE, Map.of("exerciseId", 3, "totalDistance", 5)));
        addBadgeDefinition(ActivityType.EXERCISE,
                new BadgeDefinition(6, 6, ActivityType.EXERCISE, Map.of("exerciseId", 1, "totalDistance", 30)));
        addBadgeDefinition(ActivityType.EXERCISE,
                new BadgeDefinition(7, 7, ActivityType.EXERCISE, Map.of("exerciseId", 3, "totalWeight", 5)));
        addBadgeDefinition(ActivityType.EXERCISE,
                new BadgeDefinition(8, 8, ActivityType.EXERCISE, Map.of("exerciseId", 2, "exerciseClear", 10)));

        addBadgeDefinition(ActivityType.ATTENDANCE,
                new BadgeDefinition(9, 9, ActivityType.ATTENDANCE, Map.of("targetDays", 1)));
        addBadgeDefinition(ActivityType.ATTENDANCE,
                new BadgeDefinition(10, 10, ActivityType.ATTENDANCE, Map.of("targetDays", 2)));
    }

    private void addBadgeDefinition(ActivityType type, BadgeDefinition badge) {
        badgeDefinitionMap.putIfAbsent(type, new ArrayList<>());
        badgeDefinitionMap.get(type).add(badge);
    }

    public List<BadgeDefinition> getBadgeDefinitionsByType(ActivityType activityType) {
        return badgeDefinitionMap.getOrDefault(activityType, Collections.emptyList());
    }

    public List<BadgeDefinition> getBadgeDefinitions() {
        return badgeDefinitionMap.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public Optional<BadgeDefinition> findById(int id) {
        return badgeDefinitionMap.values().stream()
                .flatMap(List::stream)
                .filter(badge -> badge.getId() == id)
                .findFirst();
    }

    public List<BadgeDefinition> findByActivityType(ActivityType activityType) {
        List<BadgeDefinition> definitions = getBadgeDefinitionsByType(activityType);
        return definitions;
    }
}
