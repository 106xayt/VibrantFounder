package no.vibrantfounder.bachelor.marketing.api.dto;

/**
 * Single KPI/metric for a platform.
 */
public record PlanMetricDto(
        String name,            // "Followers", "Engagement Rate", "Monthly Leads"
        String unit,            // "count", "%", "leads"
        Double currentValue,    // baseline
        Double targetValue,     // goal
        Integer horizonWeeks    // optional, e.g. 12
) {
}