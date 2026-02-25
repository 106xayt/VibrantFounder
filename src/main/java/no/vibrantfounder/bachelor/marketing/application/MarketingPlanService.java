package no.vibrantfounder.bachelor.marketing.application;

import jakarta.transaction.Transactional;
import no.vibrantfounder.bachelor.ai.config.AnthropicProperties;
import no.vibrantfounder.bachelor.ai.orchestration.AiCallOptions;
import no.vibrantfounder.bachelor.ai.orchestration.AiOrchestrator;
import no.vibrantfounder.bachelor.ai.orchestration.AiResult;
import no.vibrantfounder.bachelor.ai.prompting.PromptId;
import no.vibrantfounder.bachelor.marketing.api.dto.CalendarTaskDto;
import no.vibrantfounder.bachelor.marketing.api.dto.GeneratePlanRequest;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanReadResponse;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanResponse;
import no.vibrantfounder.bachelor.marketing.api.dto.PlatformPlanDto;
import no.vibrantfounder.bachelor.marketing.persistence.Assumption;
import no.vibrantfounder.bachelor.marketing.persistence.MarketingPlan;
import no.vibrantfounder.bachelor.marketing.persistence.MarketingPlanRepository;
import no.vibrantfounder.bachelor.marketing.persistence.PlatformPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MarketingPlanService {

    private final AiOrchestrator aiOrchestrator;
    private final AnthropicProperties anthropicProperties;
    private final MarketingPlanRepository marketingPlanRepository;

    public MarketingPlanService(
            AiOrchestrator aiOrchestrator,
            AnthropicProperties anthropicProperties,
            MarketingPlanRepository marketingPlanRepository
    ) {
        this.aiOrchestrator = aiOrchestrator;
        this.anthropicProperties = anthropicProperties;
        this.marketingPlanRepository = marketingPlanRepository;
    }

    // ---------------------------
    // CREATE (AI -> DB)
    // ---------------------------
    @Transactional
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

        // If AI doesn't provide todayTasks, compute it from calendar
        plan = ensureTodayTasks(plan);

        // ---- PERSIST ----
        MarketingPlan entity = toEntity(request, plan, result.rawText());
        marketingPlanRepository.save(entity);

        return plan;
    }

    private MarketingPlanResponse ensureTodayTasks(MarketingPlanResponse plan) {
        if (plan == null) return null;
        if (plan.todayTasks() != null) return plan;

        int computed = 0;
        List<CalendarTaskDto> cal = plan.calendar();
        if (cal != null && !cal.isEmpty()) {
            LocalDate today = LocalDate.now();
            computed = (int) cal.stream()
                    .filter(t -> t != null && t.date() != null && today.equals(t.date()))
                    .count();
        }

        // records are immutable -> rebuild
        return new MarketingPlanResponse(
                plan.summary(),
                plan.platformPlans(),
                plan.measurement(),
                plan.assumptions(),
                plan.confidence(),

                plan.planPeriodWeeks(),
                plan.growthPotential(),
                plan.goalProgressPct(),
                computed,

                plan.platformMetrics(),
                plan.contentIdeas(),
                plan.calendar(),

                plan.generatedAt()
        );
    }

    private MarketingPlan toEntity(GeneratePlanRequest req, MarketingPlanResponse res, String rawJson) {
        MarketingPlan mp = new MarketingPlan();
        mp.setIndustry(req.industry());
        mp.setTargetAudience(req.targetAudience());
        mp.setPrimaryGoal(req.primaryGoal() == null ? null : req.primaryGoal().name());
        mp.setResourcesPerWeek(req.resourcesPerWeek() == null ? 0 : req.resourcesPerWeek());
        mp.setRawJson(rawJson);
        mp.setGeneratedAt(LocalDateTime.now());

        if (res.platformPlans() != null) {
            for (PlatformPlanDto p : res.platformPlans()) {
                if (p == null) continue;
                PlatformPlan pp = new PlatformPlan();
                pp.setPlatform(p.platform() == null ? null : p.platform().name());
                pp.setFrequencyPerWeek(p.frequencyPerWeek());
                pp.setRationale(p.rationale());
                mp.addPlatform(pp);
            }
        }

        if (res.assumptions() != null) {
            res.assumptions().forEach(a -> {
                if (a == null) return;
                Assumption as = new Assumption();
                as.setText(a.assumption());
                as.setRiskLevel(a.riskLevel() == null ? null : a.riskLevel().name());
                as.setHowToTest(a.howToTest());
                mp.addAssumption(as);
            });
        }

        // NOTE:
        // We do NOT persist calendar/contentIdeas/metrics yet. They remain available via rawJson
        // and are returned to frontend via MarketingPlanResponse.

        return mp;
    }

    // ---------------------------
    // READ ONE (DB -> API)  (frontend-safe, no rawJson)
    // ---------------------------
    @Transactional
    public MarketingPlanReadResponse getPlan(Long id) {
        MarketingPlan plan = marketingPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MarketingPlan not found: " + id));
        return toReadResponse(plan);
    }

    // ---------------------------
    // READ ALL (DB -> API)  (paginated + optional filtering)
    // ---------------------------
    @Transactional
    public Page<MarketingPlanReadResponse> getPlans(String industry, String primaryGoal, Pageable pageable) {
        boolean hasIndustry = industry != null && !industry.trim().isBlank();
        boolean hasGoal = primaryGoal != null && !primaryGoal.trim().isBlank();

        if (hasIndustry && hasGoal) {
            return marketingPlanRepository
                    .findByIndustryContainingIgnoreCaseAndPrimaryGoalIgnoreCase(industry.trim(), primaryGoal.trim(), pageable)
                    .map(this::toReadResponse);
        }

        if (hasIndustry) {
            return marketingPlanRepository
                    .findByIndustryContainingIgnoreCase(industry.trim(), pageable)
                    .map(this::toReadResponse);
        }

        if (hasGoal) {
            return marketingPlanRepository
                    .findByPrimaryGoalIgnoreCase(primaryGoal.trim(), pageable)
                    .map(this::toReadResponse);
        }

        return marketingPlanRepository.findAll(pageable).map(this::toReadResponse);
    }

    private MarketingPlanReadResponse toReadResponse(MarketingPlan plan) {
        List<MarketingPlanReadResponse.PlatformPlanRow> platforms =
                plan.getPlatforms() == null ? List.of() :
                        plan.getPlatforms().stream()
                                .map(p -> new MarketingPlanReadResponse.PlatformPlanRow(
                                        p.getId(),
                                        p.getPlatform(),
                                        p.getFrequencyPerWeek(),
                                        p.getRationale()
                                ))
                                .toList();

        List<MarketingPlanReadResponse.AssumptionRow> assumptions =
                plan.getAssumptions() == null ? List.of() :
                        plan.getAssumptions().stream()
                                .map(a -> new MarketingPlanReadResponse.AssumptionRow(
                                        a.getId(),
                                        a.getText(),
                                        a.getRiskLevel(),
                                        a.getHowToTest()
                                ))
                                .toList();

        return new MarketingPlanReadResponse(
                plan.getId(),
                plan.getIndustry(),
                plan.getTargetAudience(),
                plan.getPrimaryGoal(),
                plan.getResourcesPerWeek(),
                plan.getGeneratedAt(),
                platforms,
                assumptions
        );
    }

    // ---------------------------
    // Validation
    // ---------------------------
    private void validateAgainstRequest(MarketingPlanResponse plan, GeneratePlanRequest request) {
        Integer resourcesPerWeek = request.resourcesPerWeek();
        if (resourcesPerWeek == null) return;

        if (resourcesPerWeek < 0) {
            throw new IllegalArgumentException("resourcesPerWeek must be >= 0");
        }

        if (plan.platformPlans() == null) return;

        for (PlatformPlanDto p : plan.platformPlans()) {
            if (p == null) continue;

            if (p.frequencyPerWeek() > resourcesPerWeek) {
                String platformName = p.platform() == null ? "UNKNOWN" : p.platform().name();
                throw new IllegalArgumentException(
                        "frequencyPerWeek (" + p.frequencyPerWeek() + ") exceeds resourcesPerWeek (" + resourcesPerWeek +
                                ") for platform " + platformName
                );
            }
        }
    }

    // ---------------------------
    // Helpers
    // ---------------------------
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