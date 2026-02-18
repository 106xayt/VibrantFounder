package no.vibrantfounder.bachelor.marketing.persistence;

import jakarta.persistence.*;

@Entity
public class Assumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String text;

    @Column(length = 255)
    private String riskLevel;

    @Column(length = 2000)
    private String howToTest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_plan_id")
    private MarketingPlan marketingPlan;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getHowToTest() { return howToTest; }
    public void setHowToTest(String howToTest) { this.howToTest = howToTest; }

    public MarketingPlan getMarketingPlan() { return marketingPlan; }
    public void setMarketingPlan(MarketingPlan marketingPlan) { this.marketingPlan = marketingPlan; }
}
