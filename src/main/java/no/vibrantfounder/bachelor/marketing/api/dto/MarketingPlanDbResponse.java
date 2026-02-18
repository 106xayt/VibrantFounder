package no.vibrantfounder.bachelor.marketing.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketingPlanDbResponse(
        Long id,
        String industry,
        String targetAudience,
        String primaryGoal,
        int resourcesPerWeek,
        LocalDateTime generatedAt,
        String rawJson,
        List<PlatformPlanRow> platforms,
        List<AssumptionRow> assumptions
) {
    public record PlatformPlanRow(
            Long id,
            String platform,
            int frequencyPerWeek,
            String rationale
    ) {}

    public record AssumptionRow(
            Long id,
            String text,
            String riskLevel,
            String howToTest
    ) {}
}
