package no.vibrantfounder.bachelor.marketing.api;

import jakarta.validation.Valid;
import no.vibrantfounder.bachelor.marketing.api.dto.GeneratePlanRequest;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanReadResponse;
import no.vibrantfounder.bachelor.marketing.api.dto.MarketingPlanResponse;
import no.vibrantfounder.bachelor.marketing.application.MarketingPlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    // READ ALL (DB)  (paginated + optional filtering)
    // GET /api/v1/marketing/plans?page=0&size=20&sort=generatedAt,desc
    // GET /api/v1/marketing/plans?industry=saas&goal=GROWTH&page=0&size=20
    // ---------------------------
    @GetMapping("/plans")
    public ResponseEntity<Page<MarketingPlanReadResponse>> getAllPlans(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String goal,
            Pageable pageable
    ) {
        Page<MarketingPlanReadResponse> plans = marketingPlanService.getPlans(industry, goal, pageable);
        return ResponseEntity.ok(plans);
    }

    // ---------------------------
    // READ ONE (DB)
    // ---------------------------
    @GetMapping("/plans/{id}")
    public ResponseEntity<MarketingPlanReadResponse> getPlanById(
            @PathVariable Long id
    ) {
        MarketingPlanReadResponse plan = marketingPlanService.getPlan(id);
        return ResponseEntity.ok(plan);
    }
}
