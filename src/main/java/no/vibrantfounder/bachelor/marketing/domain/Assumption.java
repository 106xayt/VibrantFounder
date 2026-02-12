package no.vibrantfounder.bachelor.marketing.domain;

import no.vibrantfounder.bachelor.marketing.domain.enums.RiskLevel;

/**
 * Domain object representing an explicit assumption made by the AI
 * as part of the marketing decision-support process.
 */
public record Assumption(
        String assumption,
        RiskLevel riskLevel,
        String howToTest
) {
}
