package no.vibrantfounder.bachelor.marketing.api;

import no.vibrantfounder.bachelor.marketing.api.dto.GeneratePlanRequest;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanResponse;
import no.vibrantfounder.bachelor.marketing.application.MarketingPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marketing")
public class MarketingPlanController {

    private final MarketingPlanService marketingPlanService;

    public MarketingPlanController(MarketingPlanService marketingPlanService) {
        this.marketingPlanService = marketingPlanService;
    }

    @PostMapping("/plan")
    public ResponseEntity<MarketingPlanResponse> generatePlan(@RequestBody GeneratePlanRequest request) {
        MarketingPlanResponse response = marketingPlanService.generatePlan(request);
        return ResponseEntity.ok(response);
    }
}
