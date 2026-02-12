package no.vibrantfounder.bachelor.marketing.api.dto;

import java.util.List;

/**
 * DTO representing the AI's confidence assessment for the overall plan.
 */
public record ConfidenceDto(
        double score,
        List<String> reasons
) {
}
