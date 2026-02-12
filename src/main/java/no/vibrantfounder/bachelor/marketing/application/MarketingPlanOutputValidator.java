package no.vibrantfounder.bachelor.marketing.application;

import no.vibrantfounder.bachelor.ai.orchestration.OutputValidator;
import no.vibrantfounder.bachelor.marketing.api.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarketingPlanOutputValidator implements OutputValidator<Object> {

    @Override
    public void validate(Object output) {
        if (!(output instanceof MarketingPlanResponse plan)) {
            return;
        }

        requireNotBlank(plan.summary(), "summary");

        requireNotNull(plan.platformPlans(), "platformPlans");
        requireNotEmpty(plan.platformPlans(), "platformPlans");

        requireNotNull(plan.measurement(), "measurement");
        validateMeasurement(plan.measurement());

        requireNotNull(plan.assumptions(), "assumptions");
        requireSizeBetween(plan.assumptions(), 3, 6, "assumptions");
        plan.assumptions().forEach(this::validateAssumption);

        requireNotNull(plan.confidence(), "confidence");
        validateConfidence(plan.confidence());

        requireNotBlank(plan.generatedAt(), "generatedAt");

        for (int i = 0; i < plan.platformPlans().size(); i++) {
            validatePlatformPlan(plan.platformPlans().get(i), "platformPlans[" + i + "]");
        }
    }

    private void validatePlatformPlan(PlatformPlanDto p, String path) {
        requireNotNull(p, path);

        requireNotNull(p.platform(), path + ".platform");
        requireNotBlank(p.rationale(), path + ".rationale");

        if (p.frequencyPerWeek() < 1 || p.frequencyPerWeek() > 14) {
            throw new IllegalArgumentException(path + ".frequencyPerWeek must be between 1 and 14");
        }

        requireNotNull(p.formats(), path + ".formats");
        requireNotEmpty(p.formats(), path + ".formats");

        requireNotNull(p.contentPillars(), path + ".contentPillars");
        requireAtLeast(p.contentPillars(), 3, path + ".contentPillars");
        for (int i = 0; i < p.contentPillars().size(); i++) {
            validateContentPillar(p.contentPillars().get(i), path + ".contentPillars[" + i + "]");
        }

        requireNotNull(p.hooks(), path + ".hooks");
        requireAtLeast(p.hooks(), 6, path + ".hooks");
        for (int i = 0; i < p.hooks().size(); i++) {
            requireNotBlank(p.hooks().get(i), path + ".hooks[" + i + "]");
        }

        requireNotNull(p.ctaExamples(), path + ".ctaExamples");
        requireAtLeast(p.ctaExamples(), 4, path + ".ctaExamples");
        for (int i = 0; i < p.ctaExamples().size(); i++) {
            requireNotBlank(p.ctaExamples().get(i), path + ".ctaExamples[" + i + "]");
        }
    }

    private void validateContentPillar(ContentPillarDto c, String path) {
        requireNotNull(c, path);
        requireNotBlank(c.name(), path + ".name");
        requireNotBlank(c.angle(), path + ".angle");

        requireNotNull(c.examples(), path + ".examples");
        requireNotEmpty(c.examples(), path + ".examples");
        for (int i = 0; i < c.examples().size(); i++) {
            requireNotBlank(c.examples().get(i), path + ".examples[" + i + "]");
        }
    }

    private void validateMeasurement(MeasurementDto m) {
        requireNotBlank(m.northStarMetric(), "measurement.northStarMetric");

        requireNotNull(m.kpis(), "measurement.kpis");
        requireSizeBetween(m.kpis(), 3, 6, "measurement.kpis");
        for (int i = 0; i < m.kpis().size(); i++) {
            requireNotBlank(m.kpis().get(i), "measurement.kpis[" + i + "]");
        }

        requireNotBlank(m.reportingCadence(), "measurement.reportingCadence");
    }

    private void validateAssumption(AssumptionDto a) {
        requireNotNull(a, "assumptions[]");
        requireNotBlank(a.assumption(), "assumptions[].assumption");
        requireNotNull(a.riskLevel(), "assumptions[].riskLevel");
        requireNotBlank(a.howToTest(), "assumptions[].howToTest");
    }

    private void validateConfidence(ConfidenceDto c) {
        double score = c.score();
        if (Double.isNaN(score) || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("confidence.score must be between 0.0 and 1.0");
        }

        requireNotNull(c.reasons(), "confidence.reasons");
        requireNotEmpty(c.reasons(), "confidence.reasons");
        for (int i = 0; i < c.reasons().size(); i++) {
            requireNotBlank(c.reasons().get(i), "confidence.reasons[" + i + "]");
        }
    }

    private static void requireNotNull(Object v, String field) {
        if (v == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
    }

    private static void requireNotEmpty(List<?> v, String field) {
        if (v == null || v.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
    }

    private static void requireAtLeast(List<?> v, int min, String field) {
        if (v == null || v.size() < min) {
            throw new IllegalArgumentException(field + " must have at least " + min + " items");
        }
    }

    private static void requireSizeBetween(List<?> v, int min, int max, String field) {
        if (v == null || v.size() < min || v.size() > max) {
            throw new IllegalArgumentException(field + " must have between " + min + " and " + max + " items");
        }
    }

    private static void requireNotBlank(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
