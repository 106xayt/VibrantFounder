package no.vibrantfounder.bachelor.marketing.domain;

import java.util.List;

/**
 * Domain object representing the AI's confidence assessment
 * for the overall marketing plan.
 */
public record Confidence(
        double score,
        List<String> reasons
) {
}
