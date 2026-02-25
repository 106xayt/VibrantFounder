package no.vibrantfounder.bachelor.marketing.api.dto;

import java.util.List;

/**
 * Response DTO representing the AI-generated marketing decision-support plan.
 *
 * IMPORTANT:
 * - This record is deserialized directly from the AI JSON via aiOrchestrator.callForJson(...)
 * - Ensure the AI prompt returns JSON matching these fields.
 */
public record MarketingPlanResponse(
        // Executive
        String summary,

        // Core strategy (existing)
        List<PlatformPlanDto> platformPlans,
        MeasurementDto measurement,
        List<AssumptionDto> assumptions,
        ConfidenceDto confidence,

        // NEW: UI needs these (no more dummy on frontend)
        Integer planPeriodWeeks,         // e.g. 4
        String growthPotential,          // "LOW" | "MEDIUM" | "HIGH"
        Integer goalProgressPct,         // 0..100 (tracking later)
        Integer todayTasks,              // derived or provided

        List<PlatformMetricsDto> platformMetrics,
        List<ContentIdeaDto> contentIdeas,
        List<CalendarTaskDto> calendar,

        // Existing
        String generatedAt
) {
}