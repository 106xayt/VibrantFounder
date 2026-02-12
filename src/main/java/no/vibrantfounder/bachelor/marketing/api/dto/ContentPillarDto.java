package no.vibrantfounder.bachelor.marketing.api.dto;

import java.util.List;

/**
 * DTO representing a content pillar (theme) for a platform.
 */
public record ContentPillarDto(
        String name,
        String angle,
        List<String> examples
) {
}
