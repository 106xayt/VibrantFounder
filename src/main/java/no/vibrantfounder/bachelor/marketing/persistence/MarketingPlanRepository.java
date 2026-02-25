package no.vibrantfounder.bachelor.marketing.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketingPlanRepository extends JpaRepository<MarketingPlan, Long> {

    Page<MarketingPlan> findByIndustryContainingIgnoreCase(String industry, Pageable pageable);

    Page<MarketingPlan> findByPrimaryGoalIgnoreCase(String primaryGoal, Pageable pageable);

    Page<MarketingPlan> findByIndustryContainingIgnoreCaseAndPrimaryGoalIgnoreCase(
            String industry,
            String primaryGoal,
            Pageable pageable
    );
}
