package no.vibrantfounder.bachelor.marketing.domain;

import java.util.List;

/**
 * Domain object representing a content pillar (theme)
 * used within a platform-specific strategy.
 */
public record ContentPillar(
        String name,
        String angle,
        List<String> examples
) {
}
