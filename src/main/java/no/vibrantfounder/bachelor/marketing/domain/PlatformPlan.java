package no.vibrantfounder.bachelor.marketing.domain;

import no.vibrantfounder.bachelor.marketing.domain.enums.ContentFormat;
import no.vibrantfounder.bachelor.marketing.domain.enums.Platform;

import java.util.List;

/**
 * Domain object representing a platform-specific marketing strategy.
 */
public record PlatformPlan(
        Platform platform,
        String rationale,
        int frequencyPerWeek,
        List<ContentFormat> formats,
        List<ContentPillar> contentPillars,
        List<String> hooks,
        List<String> ctaExamples
) {
}
