package no.vibrantfounder.bachelor.marketing.api.dto;

import jakarta.validation.constraints.*;
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

        @NotBlank(message = "Industry is required")
        @Size(max = 80, message = "Industry must not exceed 80 characters")
        String industry,

        @NotBlank(message = "Target audience is required")
        @Size(max = 120, message = "Target audience must not exceed 120 characters")
        String targetAudience,

        @NotNull(message = "Primary goal is required")
        Goal primaryGoal,

        @Size(max = 5, message = "You can specify up to 5 secondary goals")
        List<Goal> secondaryGoals,

        @NotEmpty(message = "At least one platform must be selected")
        @Size(max = 10, message = "You can select up to 10 platforms")
        List<Platform> platforms,

        @NotNull(message = "Resources per week is required")
        @Min(value = 1, message = "Resources per week must be at least 1")
        @Max(value = 100, message = "Resources per week must not exceed 100")
        Integer resourcesPerWeek,

        @Size(max = 40, message = "Tone must not exceed 40 characters")
        String tone,

        @Size(max = 20, message = "You can specify up to 20 constraints")
        List<
                @NotBlank(message = "Constraint cannot be blank")
                @Size(max = 120, message = "Constraint must not exceed 120 characters")
                        String> constraints
) {
}
