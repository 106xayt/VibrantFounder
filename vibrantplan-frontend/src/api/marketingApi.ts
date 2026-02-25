// app/api/marketingApi.ts

export type Goal = "AWARENESS" | "LEADS" | "SALES" | "COMMUNITY";
export type Platform = "TIKTOK" | "INSTAGRAM" | "LINKEDIN";

export interface GeneratePlanRequest {
    industry: string;
    targetAudience: string;
    primaryGoal: Goal;
    secondaryGoals?: Goal[];
    platforms: Platform[];
    resourcesPerWeek: number;
    tone?: string;
    constraints?: string[];
}

export interface ContentPillarDto {
    name: string;
    angle: string;
    examples: string[];
}

export interface PlatformPlanDto {
    platform: Platform;
    rationale: string;
    frequencyPerWeek: number;
    formats: string[];
    contentPillars: ContentPillarDto[];
    hooks: string[];
    ctaExamples: string[];
}

export interface MeasurementDto {
    northStarMetric?: string;
    leadingIndicators?: string[];
    laggingIndicators?: string[];
    reviewCadence?: string;
}

export interface AssumptionDto {
    statement?: string;
    risk?: string;
    validationAction?: string;
}

export interface ConfidenceDto {
    level?: string;
    notes?: string;
}

export interface MarketingPlanResponse {
    summary: string;
    platformPlans: PlatformPlanDto[];
    measurement?: MeasurementDto;
    assumptions?: AssumptionDto[];
    confidence?: ConfidenceDto;
    generatedAt?: string;
}

function inferGoalFromText(text: string): Goal {
    const t = (text || "").toLowerCase();
    if (t.includes("lead") || t.includes("demo") || t.includes("signup")) return "LEADS";
    if (t.includes("sale") || t.includes("revenue") || t.includes("convert")) return "SALES";
    if (t.includes("community") || t.includes("followers") || t.includes("engage")) return "COMMUNITY";
    return "AWARENESS";
}

export function buildGeneratePlanRequest(args: {
    industry: string;
    targetAudience: string;
    goalsText: string;
    resourcesPerWeek?: number;
    tone?: string;
    platforms?: Platform[];
    constraints?: string[];
}): GeneratePlanRequest {
    return {
        industry: args.industry,
        targetAudience: args.targetAudience,
        primaryGoal: inferGoalFromText(args.goalsText),
        secondaryGoals: [],
        platforms: args.platforms ?? ["LINKEDIN", "INSTAGRAM", "TIKTOK"],
        resourcesPerWeek: args.resourcesPerWeek ?? 5,
        tone: args.tone ?? "Clear, practical, founder-friendly",
        constraints: args.constraints ?? [],
    };
}

export async function generateMarketingPlan(
    request: GeneratePlanRequest
): Promise<MarketingPlanResponse> {
    const res = await fetch("/api/v1/marketing/plan", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        // Hvis dere bruker Spring Security session/cookies:
        credentials: "include",
        body: JSON.stringify(request),
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(text || `Failed to generate plan (${res.status})`);
    }

    return res.json();
}