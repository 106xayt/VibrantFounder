import { useMemo, useState } from "react";
import { Sparkles, Send, RotateCcw } from "lucide-react";
import { generateMarketingPlan } from "../api/marketingApi";
import type {
    GeneratePlanRequest,
    MarketingPlanResponse,
    Goal,
    Platform,
} from "../types";

interface Props {
    onPlanGenerated: (plan: MarketingPlanResponse) => void;
}

type Role = "user" | "ai";

type Message = {
    id: number;
    role: Role;
    text: string;
};

type Draft = {
    industry?: string;
    targetAudience?: string;
    primaryGoal?: Goal;
    secondaryGoals?: Goal[];
    platforms?: Platform[];
    resourcesPerWeek?: number;
    tone?: string;
    constraints?: string[];
};

const GOAL_KEYWORDS: Array<{ goal: Goal; keywords: string[] }> = [
    {
        goal: "LEADS" as Goal,
        keywords: ["lead", "leads", "book calls", "bookings", "appointments", "pipeline"],
    },
    {
        goal: "AWARENESS" as Goal,
        keywords: ["awareness", "brand", "reach", "top of funnel", "visibility"],
    },
    {
        goal: "SALES" as Goal,
        keywords: ["sales", "revenue", "close", "conversions", "purchase", "buy"],
    },
    {
        goal: "COMMUNITY" as Goal,
        keywords: ["community", "followers", "engagement", "loyalty", "retention"],
    },
];

const TONE_KEYWORDS: Array<{ tone: string; keywords: string[] }> = [
    { tone: "Professional", keywords: ["professional", "formal", "b2b"] },
    { tone: "Friendly", keywords: ["friendly", "warm", "approachable"] },
    { tone: "Bold", keywords: ["bold", "confident", "direct"] },
    { tone: "Playful", keywords: ["playful", "fun", "humor", "humour", "witty"] },
    { tone: "Educational", keywords: ["educational", "teach", "explain", "how to"] },
];

function normalize(s: string) {
    return s.trim().toLowerCase();
}

function uniq<T>(arr: T[]) {
    return Array.from(new Set(arr));
}

function clampInt(n: number, min: number, max: number) {
    return Math.max(min, Math.min(max, n));
}

/**
 * Tokenize to avoid substring traps (e.g. "clients" contains "li").
 */
function tokenizeWords(text: string): string[] {
    // split on anything not a letter/number
    const parts = text
        .toLowerCase()
        .split(/[^a-z0-9]+/g)
        .map((x) => x.trim())
        .filter(Boolean);

    return parts;
}

/**
 * Platform parsing with safe word-boundary rules.
 * - LinkedIn: only "linkedin" or phrase "linked in"
 * - Instagram: "instagram" or token "ig"
 * - TikTok: "tiktok" or token "tt"
 */
function parsePlatforms(raw: string): Platform[] {
    const tokens = tokenizeWords(raw);

    const platforms: Platform[] = [];

    // linked in phrase
    if (/\blinked\s+in\b/i.test(raw) || tokens.includes("linkedin")) {
        platforms.push("LINKEDIN" as Platform);
    }
    if (tokens.includes("instagram") || tokens.includes("ig")) {
        platforms.push("INSTAGRAM" as Platform);
    }
    if (tokens.includes("tiktok") || tokens.includes("tt")) {
        platforms.push("TIKTOK" as Platform);
    }

    // explicit: platforms: ...
    const platformsExplicit = raw.match(/\bplatforms?\s*[:=]\s*(.+)$/i)?.[1];
    if (platformsExplicit) {
        const expTokens = tokenizeWords(platformsExplicit);
        if (/\blinked\s+in\b/i.test(platformsExplicit) || expTokens.includes("linkedin")) {
            platforms.push("LINKEDIN" as Platform);
        }
        if (expTokens.includes("instagram") || expTokens.includes("ig")) {
            platforms.push("INSTAGRAM" as Platform);
        }
        if (expTokens.includes("tiktok") || expTokens.includes("tt")) {
            platforms.push("TIKTOK" as Platform);
        }
    }

    return uniq(platforms);
}

/**
 * Tries to extract structured fields from a natural language message.
 * Returns { patch, leftovers } where leftovers can be used for industry/audience if missing.
 */
function parseMessage(text: string): { patch: Partial<Draft>; leftovers: string } {
    const raw = text;
    const t = normalize(text);

    const patch: Partial<Draft> = {};
    let leftovers = raw;

    // --- Skip tone shortcut ---
    if (t === "skip" || t === "skip tone" || t === "no tone") {
        patch.tone = "Default";
        return { patch, leftovers: "" };
    }

    // --- Primary goal (and secondary goals) ---
    const goalExplicit =
        t.match(/\b(primary\s*goal|goal)\s*[:=]\s*([a-z\s]+)/i)?.[2]?.trim() ?? null;

    const foundGoals: Goal[] = [];

    const scanGoal = (s: string) => {
        for (const g of GOAL_KEYWORDS) {
            if (g.keywords.some((k) => s.includes(k))) foundGoals.push(g.goal);
        }
    };

    if (goalExplicit) scanGoal(goalExplicit);
    scanGoal(t);

    if (foundGoals.length > 0) {
        const distinct = uniq(foundGoals);
        patch.primaryGoal = distinct[0];
        if (distinct.length > 1) patch.secondaryGoals = distinct.slice(1, 6);
    }

    // --- Platforms (safe parsing) ---
    const platforms = parsePlatforms(raw);
    if (platforms.length > 0) patch.platforms = platforms;

    // --- Resources per week (posts/content pieces per week) ---
    // Accept patterns like:
    // "5 per week", "5/week", "5x/week", "5 posts/week", "resources/week 5"
    const res1 = t.match(/\b(\d{1,3})\s*(x|times)?\s*(per\s*week|\/week|weekly)\b/);
    const res2 = t.match(/\b(resources?|posts?|pieces?|content)\s*(per\s*week|\/week)\s*[:=]?\s*(\d{1,3})\b/);
    const res3 = t.match(/\b(\d{1,3})\s*(posts?|pieces?|content)\s*(a\s*week|per\s*week|\/week)\b/);

    const rawNum =
        (res2?.[3] ? Number(res2[3]) : null) ??
        (res3?.[1] ? Number(res3[1]) : null) ??
        (res1?.[1] ? Number(res1[1]) : null);

    if (rawNum != null && Number.isFinite(rawNum)) {
        patch.resourcesPerWeek = clampInt(rawNum, 1, 100);
    }

    // --- Tone ---
    const toneExplicit = t.match(/\btone\s*[:=]\s*([a-z\s-]+)/i)?.[1]?.trim() ?? null;
    const toneHits: string[] = [];
    if (toneExplicit) toneHits.push(toneExplicit);

    for (const tt of TONE_KEYWORDS) {
        if (tt.keywords.some((k) => t.includes(k))) toneHits.push(tt.tone);
    }
    if (toneHits.length > 0) patch.tone = toneHits[0];

    // --- Audience / Industry explicit fields ---
    const industryExplicit = raw.match(/\bindustry\s*[:=]\s*(.+)$/i)?.[1]?.trim();
    const audienceExplicit = raw.match(/\b(audience|target\s*audience)\s*[:=]\s*(.+)$/i)?.[2]?.trim();

    if (industryExplicit) patch.industry = industryExplicit;
    if (audienceExplicit) patch.targetAudience = audienceExplicit;

    // --- Constraints ---
    const constraintsExplicit = raw.match(/\bconstraints?\s*[:=]\s*(.+)$/i)?.[1]?.trim();
    const constraints: string[] = [];

    if (constraintsExplicit) {
        constraintsExplicit
            .split(",")
            .map((x) => x.trim())
            .filter(Boolean)
            .forEach((x) => constraints.push(x));
    }

    const constraintSignals = ["avoid", "no ", "can't", "cannot", "do not", "dont", "don't", "must not", "without"];
    if (constraintSignals.some((s) => t.includes(s))) {
        if (!constraintsExplicit) constraints.push(raw.trim());
    }

    if (constraints.length > 0) patch.constraints = uniq(constraints).slice(0, 20);

    // leftovers only used if no explicit industry/audience fields
    if (industryExplicit || audienceExplicit) leftovers = "";

    return { patch, leftovers };
}

function nextQuestion(d: Draft): string | null {
    if (!d.industry) return "What industry are you in? (e.g. “B2B SaaS for dentists”)";
    if (!d.targetAudience) return "Who is your target audience? (be specific)";
    if (!d.primaryGoal)
        return "What’s the primary goal? (LEADS / AWARENESS / SALES / COMMUNITY) — you can also say it in plain English.";
    if (!d.platforms || d.platforms.length === 0)
        return "Which platforms do you want to focus on? (LinkedIn, Instagram, TikTok)";
    if (!d.resourcesPerWeek)
        return "How many content pieces can you ship per week? (e.g. “5/week”)";
    if (!d.tone) return "Optional: What tone should we use? (professional, friendly, bold, playful, educational) — or say “skip”.";
    return null;
}

function buildSummary(d: Draft) {
    const platforms = (d.platforms ?? []).join(", ");
    const secondary = (d.secondaryGoals ?? []).join(", ");
    const constraints = (d.constraints ?? []).slice(0, 5).join(" • ");
    return [
        `Industry: ${d.industry ?? "-"}`,
        `Target audience: ${d.targetAudience ?? "-"}`,
        `Primary goal: ${d.primaryGoal ?? "-"}`,
        secondary ? `Secondary goals: ${secondary}` : null,
        `Platforms: ${platforms || "-"}`,
        `Resources/week: ${d.resourcesPerWeek ?? "-"}`,
        d.tone ? `Tone: ${d.tone}` : null,
        constraints ? `Constraints: ${constraints}` : null,
    ]
        .filter(Boolean)
        .join("\n");
}

export function AiChat({ onPlanGenerated }: Props) {
    const [messages, setMessages] = useState<Message[]>([
        {
            id: 1,
            role: "ai",
            text:
                "Hey 👋 Describe your business in one message (industry, audience, goal, platforms, posts/week). I’ll pick it up — and ask only what’s missing.",
        },
        { id: 2, role: "ai", text: "Let’s start: what industry are you in?" },
    ]);

    const [draft, setDraft] = useState<Draft>({});
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const readyToGenerate = useMemo(() => {
        return Boolean(
            draft.industry &&
            draft.targetAudience &&
            draft.primaryGoal &&
            draft.platforms &&
            draft.platforms.length > 0 &&
            draft.resourcesPerWeek
        );
    }, [draft]);

    function push(role: Role, text: string) {
        setMessages((prev) => [...prev, { id: Date.now() + Math.random(), role, text }]);
    }

    function resetChat() {
        setDraft({});
        setInput("");
        setError(null);
        setLoading(false);
        setMessages([
            {
                id: 1,
                role: "ai",
                text:
                    "Hey 👋 Describe your business in one message (industry, audience, goal, platforms, posts/week). I’ll pick it up — and ask only what’s missing.",
            },
            { id: 2, role: "ai", text: "Let’s start: what industry are you in?" },
        ]);
    }

    async function maybeGenerate(updatedDraft: Draft) {
        const ok =
            updatedDraft.industry &&
            updatedDraft.targetAudience &&
            updatedDraft.primaryGoal &&
            updatedDraft.platforms &&
            updatedDraft.platforms.length > 0 &&
            updatedDraft.resourcesPerWeek;

        if (!ok) return;

        const payload: GeneratePlanRequest = {
            industry: updatedDraft.industry!,
            targetAudience: updatedDraft.targetAudience!,
            primaryGoal: updatedDraft.primaryGoal!,
            secondaryGoals: updatedDraft.secondaryGoals ?? [],
            platforms: updatedDraft.platforms!,
            resourcesPerWeek: updatedDraft.resourcesPerWeek!,
            tone: (updatedDraft.tone && updatedDraft.tone !== "Default") ? updatedDraft.tone : null,
            constraints: updatedDraft.constraints ?? [],
        } as unknown as GeneratePlanRequest;

        push("ai", "Perfect. Here’s what I understood:\n\n" + buildSummary(updatedDraft));
        push("ai", "Generating your plan now… 🚀");

        setLoading(true);
        setError(null);

        try {
            const plan = await generateMarketingPlan(payload);
            onPlanGenerated(plan);
            push("ai", "Done ✅ Your plan is ready in “Your Plan”.");
        } catch (e: any) {
            let msg = "Something went wrong while generating the plan.";
            const m = String(e?.message ?? "");

            if (m.includes("401")) msg = "HTTP 401: Unauthorized. Check Basic Auth / credentials.";
            else if (m.includes("403")) msg = "HTTP 403: Forbidden. Check security config.";
            else if (m.includes("422")) msg = "HTTP 422: Validation error. Payload didn’t match backend DTO.";
            else if (m.includes("500")) msg = "HTTP 500: Server error. Check backend logs for the correlationId.";
            else if (m.toLowerCase().includes("timeout")) msg = "Request timed out. Increase backend timeout or reduce output size.";

            setError(msg);
            push("ai", `⚠️ ${msg}`);
        } finally {
            setLoading(false);
        }
    }

    async function handleSend() {
        const text = input.trim();
        if (!text || loading) return;

        setInput("");
        setError(null);
        push("user", text);

        // Special case: user only typed a number and we're missing resources/week
        const justNumber = text.match(/^\s*(\d{1,3})\s*$/);
        const numericOnly = justNumber ? Number(justNumber[1]) : null;

        const { patch, leftovers } = parseMessage(text);

        setDraft((prev) => {
            const merged: Draft = { ...prev, ...patch };

            // If user typed only a number and resourcesPerWeek is the missing required field,
            // interpret it as resources/week.
            if (!merged.resourcesPerWeek && numericOnly != null && Number.isFinite(numericOnly)) {
                const needResources = Boolean(
                    merged.industry &&
                    merged.targetAudience &&
                    merged.primaryGoal &&
                    merged.platforms &&
                    merged.platforms.length > 0
                );
                if (needResources) merged.resourcesPerWeek = clampInt(numericOnly, 1, 100);
            }

            // If user gave free text and we still miss industry/audience, use leftovers carefully:
            const leftoversClean = leftovers.trim();
            if (leftoversClean) {
                if (!merged.industry) merged.industry = leftoversClean;
                else if (!merged.targetAudience) merged.targetAudience = leftoversClean;
            }

            // Normalize / dedupe
            if (merged.platforms) merged.platforms = uniq(merged.platforms);
            if (merged.secondaryGoals) merged.secondaryGoals = uniq(merged.secondaryGoals).slice(0, 5);
            if (merged.constraints) merged.constraints = uniq(merged.constraints).slice(0, 20);

            const requiredOk =
                merged.industry &&
                merged.targetAudience &&
                merged.primaryGoal &&
                merged.platforms &&
                merged.platforms.length > 0 &&
                merged.resourcesPerWeek;

            if (!requiredOk) {
                push("ai", nextQuestion(merged) ?? "Tell me a bit more.");
            } else {
                // If tone missing, ask once but don't block generation
                if (!merged.tone) {
                    push("ai", "Optional: What tone should we use? (professional, friendly, bold, playful, educational) — or say “skip”.");
                    // Generate immediately anyway (tone optional)
                    void maybeGenerate(merged);
                } else {
                    void maybeGenerate(merged);
                }
            }

            return merged;
        });
    }

    return (
        <div className="vf-card p-6 flex flex-col h-[650px]">
            <div className="flex items-center justify-between gap-3 mb-4">
                <div className="flex items-center gap-2">
                    <Sparkles className="size-4 text-[var(--vf-orange)]" />
                    <h2 className="font-semibold tracking-tight">AI Chat</h2>
                </div>

                <button
                    type="button"
                    onClick={resetChat}
                    className="inline-flex items-center gap-2 text-sm px-3 py-2 rounded-xl border border-white/10 hover:bg-white/5 transition text-white/80"
                    disabled={loading}
                    title="Reset"
                >
                    <RotateCcw className="size-4" />
                    Reset
                </button>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto space-y-4 mb-4 pr-1">
                {messages.map((m) => (
                    <div
                        key={m.id}
                        className={`p-3 rounded-2xl text-sm whitespace-pre-wrap max-w-[88%] border ${
                            m.role === "ai"
                                ? "bg-white/5 border-white/10 text-white/85"
                                : "bg-gradient-to-r from-[var(--vf-orange)] to-[var(--vf-blue)] border-white/10 text-white ml-auto"
                        }`}
                    >
                        {m.text}
                    </div>
                ))}
            </div>

            {/* Draft helper */}
            <div className="mb-3 rounded-2xl border border-white/10 bg-white/5 p-3 text-xs text-white/70 whitespace-pre-wrap">
                <div className="flex items-center justify-between gap-2 mb-1">
                    <span className="font-semibold text-white/85">Captured so far</span>
                    <span className={`font-semibold ${readyToGenerate ? "text-emerald-400" : "text-amber-400"}`}>
            {readyToGenerate ? "Ready" : "Incomplete"}
          </span>
                </div>
                {buildSummary(draft) || "-"}
            </div>

            {/* Error */}
            {error && <div className="text-sm text-red-400 mb-3">{error}</div>}

            {/* Input */}
            <div className="flex gap-2">
                <input
                    className="vf-input"
                    placeholder={loading ? "Generating…" : "Type here… (press Enter to send)"}
                    value={input}
                    disabled={loading}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") handleSend();
                    }}
                />

                <button
                    onClick={handleSend}
                    disabled={loading || !input.trim()}
                    className="vf-gradient-btn min-w-[110px] inline-flex items-center justify-center gap-2"
                >
                    <Send className="size-4" />
                    {loading ? "Working…" : "Send"}
                </button>
            </div>

            {!loading && (
                <div className="mt-3 text-[11px] text-white/55">
                    Tip: You can write:{" "}
                    <span className="font-semibold">
            “B2B SaaS for dentists. Audience: clinic owners. Goal: leads. Platforms: LinkedIn + IG. 5/week.”
          </span>
                </div>
            )}
        </div>
    );
}