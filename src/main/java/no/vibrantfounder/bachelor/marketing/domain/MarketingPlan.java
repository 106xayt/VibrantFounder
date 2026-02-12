package no.vibrantfounder.bachelor.marketing.domain;

import java.util.List;

/**
 * Domain object representing the complete marketing decision-support plan.
 */
public record MarketingPlan(
        String summary,
        List<PlatformPlan> platformPlans,
        Measurement measurement,
        List<Assumption> assumptions,
        Confidence confidence,
        String generatedAt
) {
}
