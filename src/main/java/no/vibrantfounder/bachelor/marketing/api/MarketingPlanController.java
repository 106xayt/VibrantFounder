package no.vibrantfounder.bachelor.marketing.api;

import jakarta.validation.Valid;
import no.vibrantfounder.bachelor.marketing.api.dto.GeneratePlanRequest;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanDbResponse;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanResponse;
import no.vibrantfounder.bachelor.marketing.application.MarketingPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marketing")
public class MarketingPlanController {

    private final MarketingPlanService marketingPlanService;

    public MarketingPlanController(MarketingPlanService marketingPlanService) {
        this.marketingPlanService = marketingPlanService;
    }

    // ---------------------------
    // CREATE (AI → DB)
    // ---------------------------
    @PostMapping("/plan")
    public ResponseEntity<MarketingPlanResponse> generatePlan(
            @Valid @RequestBody GeneratePlanRequest request
    ) {
        MarketingPlanResponse response = marketingPlanService.generatePlan(request);
        return ResponseEntity.ok(response);
    }

    // ---------------------------
    // READ ALL (DB)
    // ---------------------------
    @GetMapping("/plans")
    public ResponseEntity<List<MarketingPlanDbResponse>> getAllPlans() {
        List<MarketingPlanDbResponse> plans = marketingPlanService.getPlans();
        return ResponseEntity.ok(plans);
    }

    // ---------------------------
    // READ ONE (DB)
    // ---------------------------
    @GetMapping("/plans/{id}")
    public ResponseEntity<MarketingPlanDbResponse> getPlanById(
            @PathVariable Long id
    ) {
        MarketingPlanDbResponse plan = marketingPlanService.getPlan(id);
        return ResponseEntity.ok(plan);
    }
}
