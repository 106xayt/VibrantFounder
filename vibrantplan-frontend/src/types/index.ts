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

export type RiskLevel = "HIGH" | "MEDIUM" | "LOW" | string;

export type ContentPillar = {
    name: string;
    angle: string;
    examples: string[];
};

export type PlatformPlan = {
    platform: Platform | string;
    rationale: string;
    frequencyPerWeek: number;
    formats: string[];
    contentPillars: ContentPillar[];
    hooks: string[];
    ctaExamples: string[];
};

export type Measurement = {
    northStarMetric: string;
    kpis: string[];
    reportingCadence: string;
};

export type Assumption = {
    assumption: string;
    riskLevel: RiskLevel;
    howToTest: string;
};

export type Confidence = {
    score: number;
    reasons: string[];
};

/**
 * API response from /api/v1/marketing/plan
 * NOTE: Some fields are nullable depending on prompt/output.
 */
export type MarketingPlanResponse = {
    summary: string;
    platformPlans: PlatformPlan[];
    measurement: Measurement;
    assumptions: Assumption[];
    confidence: Confidence;

    planPeriodWeeks?: number | null;
    growthPotential?: number | null;
    goalProgressPct?: number | null;
    todayTasks?: number | null;
    platformMetrics?: unknown | null;
    contentIdeas?: unknown | null;
    calendar?: unknown | null;

    generatedAt: string;
};