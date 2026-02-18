package no.vibrantfounder.bachelor.marketing.persistence;

import jakarta.persistence.*;

@Entity
public class PlatformPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String platform;
    private int frequencyPerWeek;

    @Column(length = 2000)
    private String rationale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_plan_id")
    private MarketingPlan marketingPlan;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public int getFrequencyPerWeek() { return frequencyPerWeek; }
    public void setFrequencyPerWeek(int frequencyPerWeek) { this.frequencyPerWeek = frequencyPerWeek; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public MarketingPlan getMarketingPlan() { return marketingPlan; }
    public void setMarketingPlan(MarketingPlan marketingPlan) { this.marketingPlan = marketingPlan; }
}
