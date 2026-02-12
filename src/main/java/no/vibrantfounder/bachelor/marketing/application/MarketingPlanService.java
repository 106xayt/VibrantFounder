package no.vibrantfounder.bachelor.marketing.application;

import no.vibrantfounder.bachelor.ai.config.AnthropicProperties;
import no.vibrantfounder.bachelor.ai.orchestration.AiCallOptions;
import no.vibrantfounder.bachelor.ai.orchestration.AiOrchestrator;
import no.vibrantfounder.bachelor.ai.orchestration.AiResult;
import no.vibrantfounder.bachelor.ai.prompting.PromptId;
import no.vibrantfounder.bachelor.marketing.api.dto.GeneratePlanRequest;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanResponse;
import no.vibrantfounder.bachelor.marketing.api.dto.PlatformPlanDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MarketingPlanService {

    private final AiOrchestrator aiOrchestrator;
    private final AnthropicProperties anthropicProperties;

    public MarketingPlanService(AiOrchestrator aiOrchestrator, AnthropicProperties anthropicProperties) {
        this.aiOrchestrator = aiOrchestrator;
        this.anthropicProperties = anthropicProperties;
    }

    public MarketingPlanResponse generatePlan(GeneratePlanRequest request) {
        Map<String, String> vars = Map.of(
                "industry", safe(request.industry()),
                "targetAudience", safe(request.targetAudience()),
                "primaryGoal", request.primaryGoal() == null ? "" : request.primaryGoal().name(),
                "secondaryGoals", csvEnumNames(request.secondaryGoals()),
                "platforms", csvEnumNames(request.platforms()),
                "resourcesPerWeek", request.resourcesPerWeek() == null ? "" : String.valueOf(request.resourcesPerWeek()),
                "tone", safe(request.tone()),
                "constraints", csvStrings(request.constraints())
        );

        AiCallOptions options = new AiCallOptions(
                anthropicProperties.model(),
                anthropicProperties.maxTokens(),
                anthropicProperties.temperature()
        );

        AiResult<MarketingPlanResponse> result = aiOrchestrator.callForJson(
                PromptId.MARKETING_PLAN_V1,
                vars,
                MarketingPlanResponse.class,
                options
        );

        MarketingPlanResponse plan = result.value();
        validateAgainstRequest(plan, request);

        return plan;
    }

    /**
     * Cross-validations that require BOTH request + AI response.
     *
     * IMPORTANT: resourcesPerWeek = number of content pieces per week (NOT hours).
     */
    private void validateAgainstRequest(MarketingPlanResponse plan, GeneratePlanRequest request) {
        Integer resourcesPerWeek = request.resourcesPerWeek();
        if (resourcesPerWeek == null) {
            return;
        }

        if (resourcesPerWeek < 0) {
            throw new IllegalArgumentException("resourcesPerWeek must be >= 0");
        }

        if (plan.platformPlans() == null) {
            return; // structural checks are handled by OutputValidator
        }

        for (PlatformPlanDto p : plan.platformPlans()) {
            if (p == null) {
                continue;
            }

            if (p.frequencyPerWeek() > resourcesPerWeek) {
                String platformName = p.platform() == null ? "UNKNOWN" : p.platform().name();
                throw new IllegalArgumentException(
                        "frequencyPerWeek (" + p.frequencyPerWeek() + ") exceeds resourcesPerWeek (" + resourcesPerWeek +
                                ") for platform " + platformName
                );
            }
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String csvStrings(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        return values.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private static String csvEnumNames(List<? extends Enum<?>> values) {
        if (values == null || values.isEmpty()) return "";
        return values.stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
