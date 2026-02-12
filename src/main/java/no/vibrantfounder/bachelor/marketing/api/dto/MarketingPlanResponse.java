package no.vibrantfounder.bachelor.marketing.api.dto;

import java.util.List;

/**
 * Response DTO representing the AI-generated marketing decision-support plan.
 */
public record MarketingPlanResponse(
        String summary,
        List<PlatformPlanDto> platformPlans,
        MeasurementDto measurement,
        List<AssumptionDto> assumptions,
        ConfidenceDto confidence,
        String generatedAt
) {
}
