package no.vibrantfounder.bachelor.marketing.api.dto;

import no.vibrantfounder.bachelor.marketing.domain.enums.RiskLevel;

/**
 * DTO representing an explicit assumption made by the AI
 * as part of the decision support output.
 */
public record AssumptionDto(
        String assumption,
        RiskLevel riskLevel,
        String howToTest
) {
}
