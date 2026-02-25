// src/types/index.ts

export type Goal = "LEADS" | "AWARENESS" | "SALES" | "COMMUNITY";
export type Platform = "INSTAGRAM" | "TIKTOK" | "LINKEDIN";

export type GeneratePlanRequest = {
    industry: string;
    targetAudience: string;
    primaryGoal: Goal;

    // optional fordi backend ikke har @NotNull på secondaryGoals
    secondaryGoals?: Goal[];

    platforms: Platform[];
    resourcesPerWeek: number;

    // optional fordi backend ikke har @NotBlank/@NotNull på tone
    tone?: string;

    // optional fordi backend ikke har @NotNull på constraints
    constraints?: string[];
};

export type PlatformPlan = {
    id: number;
    platform: Platform | string;
    frequencyPerWeek: number;
    rationale: string;
};

export type Assumption = {
    id: number;
    text: string;
    riskLevel: "HIGH" | "MEDIUM" | "LOW" | string;
    howToTest: string;
};

export type MarketingPlanResponse = {
    id: number;
    industry: string;
    targetAudience: string;
    primaryGoal: Goal | string;
    resourcesPerWeek: number;
    generatedAt: string;
    rawJson: string;
    platformPlans: PlatformPlan[];
    assumptions: Assumption[];
};