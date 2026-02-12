package no.vibrantfounder.bachelor.marketing.domain;

import java.util.List;

/**
 * Domain object representing how marketing performance is measured.
 */
public record Measurement(
        String northStarMetric,
        List<String> kpis,
        String reportingCadence
) {
}
