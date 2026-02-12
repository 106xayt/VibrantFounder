package no.vibrantfounder.bachelor.marketing.api.dto;

import no.vibrantfounder.bachelor.marketing.domain.enums.Goal;
import no.vibrantfounder.bachelor.marketing.domain.enums.Platform;

import java.util.List;

/**
 * Request DTO for generating a marketing decision-support plan.
 *
 * Note:
 * - resourcesPerWeek represents the number of content pieces that can be produced per week,
 *   not hours or budget.
 */
public record GeneratePlanRequest(
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
