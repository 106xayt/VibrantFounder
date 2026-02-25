package no.vibrantfounder.bachelor.marketing.api.dto;

import java.util.List;

/**
 * Metrics/KPIs for a specific platform.
 */
public record PlatformMetricsDto(
        String platform,            // "LINKEDIN", "INSTAGRAM", ...
        String description,         // optional subtitle
        List<PlanMetricDto> metrics
) {
}