package no.vibrantfounder.bachelor.marketing.application;

import no.vibrantfounder.bachelor.marketing.api.dto.AssumptionDto;
import no.vibrantfounder.bachelor.marketing.api.dto.CalendarTaskDto;
import no.vibrantfounder.bachelor.marketing.api.dto.ContentIdeaDto;
import no.vibrantfounder.bachelor.marketing.api.dto.PlatformMetricsDto;
import no.vibrantfounder.bachelor.marketing.api.dto.PlatformPlanDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMarketingPlanContract {

    private String summary;

    private Integer planPeriodWeeks;

    /**
     * "LOW" | "MEDIUM" | "HIGH"
     */
    private String growthPotential;

    /**
     * Start at 0, becomes real when you add persistence for completed tasks.
     */
    private Integer goalProgressPct;

    private List<PlatformPlanDto> platformPlans;

    private List<PlatformMetricsDto> platformMetrics;

    private List<ContentIdeaDto> contentIdeas;

    private List<CalendarTaskDto> calendar;

    private List<AssumptionDto> assumptions;
}
