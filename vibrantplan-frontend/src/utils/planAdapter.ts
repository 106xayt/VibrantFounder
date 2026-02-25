import type { MarketingPlanResponse } from "../types";

export type PlanVM = {
    summary?: string;

    stats: {
        todayTasks: number | null;
        growthPotential: string | null;
        planPeriodWeeks: number | null;
        goalProgressPct: number | null;
    };

    platforms: Array<{
        platform: string;
        frequencyPerWeek: number;
        rationale?: string;

        formats?: string[];
        hooks?: string[];
        ctas?: string[];
        pillars?: Array<{ name: string; angle?: string; examples?: string[] }>;
    }>;

    assumptions: Array<{
        text: string;
        riskLevel?: string;
        howToTest?: string;
    }>;

    // Real backend-derived (only if present in rawJson)
    calendar: Array<{
        date?: string;        // ISO date if backend provides
        day?: number;         // fallback if backend provides day number
        platform: string;
        label: string;
        time?: string;
        type?: string;
    }> | null;

    contentIdeas: Array<{
        id?: string;
        type?: string;
        title: string;
        description?: string;
        platforms?: string[];
        duration?: string;
        targetAudience?: string;
        script?: string[] | string;
        productionNotes?: string[] | string;
        cta?: string;
    }> | null;

    platformMetrics: any | null; // we keep it flexible until your schema is fixed
};

function safeJsonParse(raw?: string): any | null {
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
}

function pickFirstString(...vals: any[]): string | undefined {
    for (const v of vals) {
        if (typeof v === "string" && v.trim()) return v.trim();
    }
    return undefined;
}

function toNumberOrNull(v: any): number | null {
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
}

function normalizePlatform(p: any): string {
    return String(p || "").toUpperCase();
}

function getTodayISO() {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
}

export function adaptPlanToVM(plan: MarketingPlanResponse): PlanVM {
    const raw = safeJsonParse(plan.rawJson);

    // Try to read richer data from rawJson (if your AI already sends it)
    const rawPlatformPlans: any[] =
        raw?.platformPlans || raw?.platforms || raw?.plan?.platformPlans || [];

    const platforms =
        rawPlatformPlans.length > 0
            ? rawPlatformPlans.map((p: any) => ({
                platform: normalizePlatform(p.platform || p.name),
                frequencyPerWeek: Number(p.frequencyPerWeek ?? p.frequency ?? 0) || 0,
                rationale: pickFirstString(p.rationale, p.why),

                formats: Array.isArray(p.formats) ? p.formats.map(String) : undefined,
                hooks: Array.isArray(p.hooks) ? p.hooks.map(String) : undefined,
                ctas: Array.isArray(p.ctaExamples)
                    ? p.ctaExamples.map(String)
                    : Array.isArray(p.ctas)
                        ? p.ctas.map(String)
                        : undefined,
                pillars: Array.isArray(p.contentPillars)
                    ? p.contentPillars.map((c: any) => ({
                        name: pickFirstString(c.name) || "Pillar",
                        angle: pickFirstString(c.angle),
                        examples: Array.isArray(c.examples) ? c.examples.map(String) : undefined,
                    }))
                    : undefined,
            }))
            : (plan.platformPlans || []).map((p: any) => ({
                platform: normalizePlatform(p.platform),
                frequencyPerWeek: Number(p.frequencyPerWeek ?? 0) || 0,
                rationale: p.rationale || undefined,
            }));

    const assumptions =
        (plan.assumptions || []).map((a: any) => ({
            text: a.text || "",
            riskLevel: a.riskLevel,
            howToTest: a.howToTest,
        })) || [];

    // ✅ Stats ONLY from backend/rawJson if available — otherwise null (no dummy)
    const growthPotential =
        pickFirstString(raw?.confidence?.level, raw?.growthPotential, raw?.stats?.growthPotential) ?? null;

    const planPeriodWeeks =
        toNumberOrNull(raw?.measurement?.planPeriodWeeks ?? raw?.stats?.planPeriodWeeks ?? raw?.planPeriodWeeks);

    const goalProgressPct =
        toNumberOrNull(raw?.tracking?.goalProgressPct ?? raw?.stats?.goalProgressPct ?? raw?.goalProgressPct);

    const calendarRaw =
        raw?.calendar || raw?.contentCalendar || raw?.execution?.calendar || null;

    const calendar: PlanVM["calendar"] =
        Array.isArray(calendarRaw)
            ? calendarRaw.map((t: any) => ({
                date: typeof t.date === "string" ? t.date : undefined,
                day: t.day != null ? Number(t.day) : undefined,
                platform: normalizePlatform(t.platform),
                label: String(t.label || t.title || "Task"),
                time: typeof t.time === "string" ? t.time : undefined,
                type: typeof t.type === "string" ? t.type : undefined,
            }))
            : null;

    const contentIdeasRaw =
        raw?.contentIdeas || raw?.assets || raw?.execution?.contentIdeas || null;

    const contentIdeas: PlanVM["contentIdeas"] =
        Array.isArray(contentIdeasRaw)
            ? contentIdeasRaw.map((c: any) => ({
                id: c.id ? String(c.id) : undefined,
                type: c.type ? String(c.type) : undefined,
                title: String(c.title || "Untitled idea"),
                description: typeof c.description === "string" ? c.description : undefined,
                platforms: Array.isArray(c.platforms) ? c.platforms.map(String) : undefined,
                duration: typeof c.duration === "string" ? c.duration : undefined,
                targetAudience: typeof c.targetAudience === "string" ? c.targetAudience : undefined,
                script: c.script,
                productionNotes: c.productionNotes,
                cta: typeof c.cta === "string" ? c.cta : undefined,
            }))
            : null;

    const platformMetrics =
        raw?.platformMetrics || raw?.metrics || raw?.kpis || null;

    // todayTasks only if calendar provides date (or if you later add it)
    let todayTasks: number | null = null;
    if (calendar) {
        const today = getTodayISO();
        todayTasks = calendar.filter((t) => t.date === today).length;
    }

    return {
        summary: pickFirstString(raw?.summary, raw?.planSummary),
        stats: {
            todayTasks,
            growthPotential,
            planPeriodWeeks,
            goalProgressPct,
        },
        platforms,
        assumptions,
        calendar,
        contentIdeas,
        platformMetrics,
    };
}