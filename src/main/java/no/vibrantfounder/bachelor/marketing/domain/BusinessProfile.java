package no.vibrantfounder.bachelor.marketing.domain;

import no.vibrantfounder.bachelor.marketing.domain.enums.Goal;
import no.vibrantfounder.bachelor.marketing.domain.enums.Platform;

import java.util.List;

/**
 * Domain object representing the business context
 * used as input for marketing decision support.
 */
public record BusinessProfile(
        String industry,
        String targetAudience,
        Goal primaryGoal,
        List<Goal> secondaryGoals,
        List<Platform> platforms,
        Integer resourcesPerWeek,
        String tone,
        List<String> constraints
) {
}
