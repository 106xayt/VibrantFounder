package no.vibrantfounder.bachelor.marketing.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketingPlanReadResponse(
        Long id,
        String industry,
        String targetAudience,
        String primaryGoal,
        Integer resourcesPerWeek,
        LocalDateTime generatedAt,
        List<PlatformPlanRow> platforms,
        List<AssumptionRow> assumptions
) {
    public record PlatformPlanRow(
            Long id,
            String platform,
            Integer frequencyPerWeek,
            String rationale
    ) {}

    public record AssumptionRow(
            Long id,
            String assumption,
            String riskLevel,
            String howToTest
    ) {}
}
