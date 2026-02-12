package no.vibrantfounder.bachelor.marketing.api.dto;

import java.util.List;

/**
 * DTO representing how marketing performance should be measured.
 */
public record MeasurementDto(
        String northStarMetric,
        List<String> kpis,
        String reportingCadence
) {
}
