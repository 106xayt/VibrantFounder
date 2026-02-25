package no.vibrantfounder.bachelor.marketing.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "platform_plan")
public class PlatformPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform")
    private String platform;

    @Column(name = "frequency_per_week", nullable = false)
    private int frequencyPerWeek;

    @Column(name = "rationale", length = 2000)
    private String rationale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_plan_id", nullable = false)
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
