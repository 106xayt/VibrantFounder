import { useMemo, useState } from "react";
import { Sparkles, Send, RotateCcw } from "lucide-react";
import { generateMarketingPlan } from "../api/marketingApi";
import type { GeneratePlanRequest, MarketingPlanResponse, Goal, Platform } from "../types";

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
    { goal: "LEADS" as Goal, keywords: ["lead", "leads", "book calls", "bookings", "appointments", "pipeline"] },
    { goal: "AWARENESS" as Goal, keywords: ["awareness", "brand", "reach", "top of funnel", "visibility"] },
    { goal: "SALES" as Goal, keywords: ["sales", "revenue", "close", "conversions", "purchase", "buy"] },
    { goal: "COMMUNITY" as Goal, keywords: ["community", "followers", "engagement", "loyalty", "retention"] },
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

function tokenizeWords(text: string): string[] {
    return text
        .toLowerCase()
        .split(/[^a-z0-9]+/g)
        .map((x) => x.trim())
        .filter(Boolean);
}

function parsePlatforms(raw: string): Platform[] {
    const tokens = tokenizeWords(raw);
    const platforms: Platform[] = [];

    if (/\blinked\s+in\b/i.test(raw) || tokens.includes("linkedin")) platforms.push("LINKEDIN" as Platform);
    if (tokens.includes("instagram") || tokens.includes("ig")) platforms.push("INSTAGRAM" as Platform);
    if (tokens.includes("tiktok") || tokens.includes("tt")) platforms.push("TIKTOK" as Platform);

    const platformsExplicit = raw.match(/\bplatforms?\s*[:=]\s*(.+)$/i)?.[1];
    if (platformsExplicit) {
        const expTokens = tokenizeWords(platformsExplicit);
        if (/\blinked\s+in\b/i.test(platformsExplicit) || expTokens.includes("linkedin")) platforms.push("LINKEDIN" as Platform);
        if (expTokens.includes("instagram") || expTokens.includes("ig")) platforms.push("INSTAGRAM" as Platform);
        if (expTokens.includes("tiktok") || expTokens.includes("tt")) platforms.push("TIKTOK" as Platform);
    }

    return uniq(platforms);
}

function parseMessage(text: string): { patch: Draft; leftovers: string } {
    const patch: Draft = {};
    let leftovers = text;

    // ---- 1) Explicit fields (same as before) ----
    const industryMatch = text.match(/\bindustry\s*[:=]\s*([^\n,]+)/i);
    if (industryMatch?.[1]) {
        patch.industry = industryMatch[1].trim();
        leftovers = leftovers.replace(industryMatch[0], "");
    }

    const audienceMatch = text.match(/\b(audience|target audience|target)\s*[:=]\s*([^\n,]+)/i);
    if (audienceMatch?.[2]) {
        patch.targetAudience = audienceMatch[2].trim();
        leftovers = leftovers.replace(audienceMatch[0], "");
    }

    const resourcesMatch = text.match(/\b(resources|resources\/week|per week|\/week)\s*[:=]?\s*(\d+)\b/i);
    if (resourcesMatch?.[2]) {
        patch.resourcesPerWeek = clampInt(parseInt(resourcesMatch[2], 10), 1, 50);
        leftovers = leftovers.replace(resourcesMatch[0], "");
    } else {
        const weeklyMatch = text.match(/\b(\d{1,2})\s*(\/|per)\s*week\b/i);
        if (weeklyMatch?.[1]) {
            patch.resourcesPerWeek = clampInt(parseInt(weeklyMatch[1], 10), 1, 50);
            leftovers = leftovers.replace(weeklyMatch[0], "");
        }
    }

    const platforms = parsePlatforms(text);
    if (platforms.length) patch.platforms = platforms;

    const lower = normalize(text);

    for (const rule of GOAL_KEYWORDS) {
        if (rule.keywords.some((k) => lower.includes(k))) {
            patch.primaryGoal = rule.goal;
            break;
        }
    }

    for (const rule of TONE_KEYWORDS) {
        if (rule.keywords.some((k) => lower.includes(k))) {
            patch.tone = rule.tone;
            break;
        }
    }

    // ---- 2) NEW: Infer industry from first clause if missing ----
    if (!patch.industry) {
        // Take everything before "Audience:" or "Goal:" or "Platforms:" or first period
        const candidate = text
            .split(/(?:\bAudience\b\s*:|\bTarget audience\b\s*:|\bGoal\b\s*:|\bPlatforms?\b\s*:|\.)/i)[0]
            ?.trim();

        // If it looks meaningful, use it as industry
        if (candidate && candidate.length >= 6) {
            patch.industry = candidate.replace(/^["'“”]+|["'“”]+$/g, "").trim();
        }
    }

    leftovers = leftovers.replace(/\s+/g, " ").trim();
    return { patch, leftovers };
}

function buildSummary(draft: Draft) {
    const lines: string[] = [];
    if (draft.industry) lines.push(`Industry: ${draft.industry}`);
    if (draft.targetAudience) lines.push(`Target audience: ${draft.targetAudience}`);
    if (draft.primaryGoal) lines.push(`Primary goal: ${draft.primaryGoal}`);
    if (draft.platforms?.length) lines.push(`Platforms: ${draft.platforms.join(", ")}`);
    if (draft.resourcesPerWeek) lines.push(`Resources/week: ${draft.resourcesPerWeek}`);
    if (draft.tone) lines.push(`Tone: ${draft.tone}`);
    return lines.join("\n");
}

export function AiChat({ onPlanGenerated }: Props) {
    const [messages, setMessages] = useState<Message[]>([
        {
            id: 1,
            role: "ai",
            text:
                "Tell me about your business and goals.\nExample: “B2B SaaS for dentists. Audience: clinic owners. Goal: leads. Platforms: LinkedIn + IG. 5/week.”",
        },
    ]);

    const [draft, setDraft] = useState<Draft>({});
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const readyToGenerate = useMemo(() => {
        return !!(
            draft.industry &&
            draft.targetAudience &&
            draft.primaryGoal &&
            draft.platforms?.length &&
            draft.resourcesPerWeek
        );
    }, [draft]);

    function reset() {
        setMessages([
            {
                id: 1,
                role: "ai",
                text:
                    "Tell me about your business and goals.\nExample: “B2B SaaS for dentists. Audience: clinic owners. Goal: leads. Platforms: LinkedIn + IG. 5/week.”",
            },
        ]);
        setDraft({});
        setInput("");
        setError(null);
        setLoading(false);
    }

    async function generate() {
        const request: GeneratePlanRequest = {
            industry: draft.industry!,
            targetAudience: draft.targetAudience!,
            primaryGoal: draft.primaryGoal!,
            secondaryGoals: draft.secondaryGoals ?? [],
            platforms: draft.platforms!,
            resourcesPerWeek: draft.resourcesPerWeek!,
            tone: draft.tone ?? "Professional",
            constraints: draft.constraints ?? [],
        };

        setLoading(true);
        setError(null);

        try {
            // ✅ Fix: ensure we pass the type from ../types, not the one declared in api file
            const plan = (await generateMarketingPlan(request)) as unknown as MarketingPlanResponse;

            onPlanGenerated(plan);

            setMessages((prev) => [
                ...prev,
                { id: Date.now(), role: "ai", text: "Done ✅ Your plan is ready on the right." },
            ]);
        } catch (e: any) {
            setError(e?.message ?? "Something went wrong");
        } finally {
            setLoading(false);
        }
    }

    // ✅ Fix: not async (no ignored promise warning)
    function handleSend() {
        const text = input.trim();
        if (!text) return;

        setMessages((prev) => [...prev, { id: Date.now(), role: "user", text }]);
        setInput("");

        const { patch } = parseMessage(text);
        setDraft((prev) => ({ ...prev, ...patch }));

        setMessages((prev) => [
            ...prev,
            {
                id: Date.now() + 1,
                role: "ai",
                text:
                    "Got it. I’ll capture what I can from that.\nWhen the draft is complete, hit Generate.",
            },
        ]);
    }

    return (
        <div className="vf-card p-6 h-[600px] flex flex-col">
            {/* Header */}
            <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-3">
                    <div className="size-9 rounded-xl bg-gradient-to-br from-orange-500 via-pink-500 to-purple-600 flex items-center justify-center shadow-lg">
                        <Sparkles className="size-5 text-white" />
                    </div>
                    <div>
                        <h2 className="font-bold text-lg leading-none">AI Chat</h2>
                        <p className="text-[11px] text-slate-400 -mt-0.5">Your co-founder session</p>
                    </div>
                </div>

                <button
                    onClick={reset}
                    className="inline-flex items-center gap-2 text-sm text-slate-500 hover:text-slate-700"
                    title="Reset"
                >
                    <RotateCcw className="size-4" />
                    Reset
                </button>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto pr-2">
                <div className="space-y-3">
                    {messages.map((m) => (
                        <div
                            key={m.id}
                            className={`p-3 rounded-2xl text-sm whitespace-pre-wrap max-w-[88%] ${
                                m.role === "ai"
                                    ? "bg-slate-100 text-slate-700"
                                    : "bg-gradient-to-r from-orange-500 via-pink-500 to-purple-600 text-white ml-auto"
                            }`}
                        >
                            {m.text}
                        </div>
                    ))}
                </div>
            </div>

            {/* Draft helper */}
            <div className="mt-4 mb-3 rounded-2xl border border-black/10 bg-white/70 p-3 text-xs text-slate-600 whitespace-pre-wrap">
                <div className="flex items-center justify-between gap-2 mb-1">
                    <span className="font-semibold text-slate-700">Captured so far</span>
                    <span className={`font-semibold ${readyToGenerate ? "text-emerald-600" : "text-amber-600"}`}>
            {readyToGenerate ? "Ready" : "Incomplete"}
          </span>
                </div>
                {buildSummary(draft) || "-"}
            </div>

            {/* Error */}
            {error && <div className="text-sm text-red-500 mb-3">{error}</div>}

            {/* Actions */}
            <div className="flex gap-2 mb-3">
                <button
                    onClick={generate}
                    disabled={!readyToGenerate || loading}
                    className={`vf-gradient-btn flex-1 inline-flex items-center justify-center gap-2 ${
                        !readyToGenerate || loading ? "opacity-60 cursor-not-allowed" : ""
                    }`}
                >
                    {loading ? "Generating…" : "Generate plan"}
                </button>
            </div>

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
                <div className="mt-3 text-[11px] text-slate-500">
                    Tip: You can write:{" "}
                    <span className="font-semibold">
            “B2B SaaS for dentists. Audience: clinic owners. Goal: leads. Platforms: LinkedIn + IG. 5/week.”
          </span>
                </div>
            )}
        </div>
    );
}