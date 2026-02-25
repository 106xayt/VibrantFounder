package no.vibrantfounder.bachelor.marketing.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "marketing_plan")
public class MarketingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "industry")
    private String industry;

    @Column(name = "target_audience")
    private String targetAudience;

    @Column(name = "primary_goal")
    private String primaryGoal;

    @Column(name = "resources_per_week", nullable = false)
    private int resourcesPerWeek;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "raw_json", columnDefinition = "CLOB")
    private String rawJson;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @OneToMany(mappedBy = "marketingPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatformPlan> platforms = new ArrayList<>();

    @OneToMany(mappedBy = "marketingPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assumption> assumptions = new ArrayList<>();

    // Helpers
    public void addPlatform(PlatformPlan p) {
        if (p == null) return;
        p.setMarketingPlan(this);
        this.platforms.add(p);
    }

    public void addAssumption(Assumption a) {
        if (a == null) return;
        a.setMarketingPlan(this);
        this.assumptions.add(a);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getPrimaryGoal() { return primaryGoal; }
    public void setPrimaryGoal(String primaryGoal) { this.primaryGoal = primaryGoal; }

    public int getResourcesPerWeek() { return resourcesPerWeek; }
    public void setResourcesPerWeek(int resourcesPerWeek) { this.resourcesPerWeek = resourcesPerWeek; }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public List<PlatformPlan> getPlatforms() { return platforms; }
    public void setPlatforms(List<PlatformPlan> platforms) { this.platforms = platforms; }

    public List<Assumption> getAssumptions() { return assumptions; }
    public void setAssumptions(List<Assumption> assumptions) { this.assumptions = assumptions; }
}
