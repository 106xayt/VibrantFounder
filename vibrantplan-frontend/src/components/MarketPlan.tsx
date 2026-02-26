import { useMemo, useState } from "react";
import { BarChart3, CheckCircle2, Flame, LayoutGrid, LineChart, Target } from "lucide-react";
import type { MarketingPlanResponse, PlatformPlan } from "../types";

type Props = {
    plan: MarketingPlanResponse | null;
};

type Tab = "platforms" | "measurement" | "assumptions" | "confidence";

function TabButton({
                       active,
                       onClick,
                       icon,
                       label,
                   }: {
    active: boolean;
    onClick: () => void;
    icon: React.ReactNode;
    label: string;
}) {
    return (
        <button
            onClick={onClick}
            className={`inline-flex items-center gap-2 px-3 py-2 rounded-xl border text-sm transition ${
                active
                    ? "bg-white/10 border-white/20 text-white"
                    : "bg-white/5 border-white/10 text-white/75 hover:bg-white/10"
            }`}
        >
            <span className="opacity-90">{icon}</span>
            {label}
        </button>
    );
}

function PlatformCard({ p }: { p: PlatformPlan }) {
    return (
        <div className="vf-card p-5">
            <div className="flex items-start justify-between gap-4">
                <div>
                    <div className="text-xs text-white/55">Platform</div>
                    <div className="text-xl font-semibold tracking-tight">{p.platform}</div>
                </div>
                <div className="vf-pill">
                    <Target className="size-4" />
                    {p.frequencyPerWeek}/week
                </div>
            </div>

            <div className="mt-3 text-sm text-white/75 leading-relaxed">
                <span className="text-white/55">Rationale:</span> {p.rationale}
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
                {p.formats?.map((f) => (
                    <span key={f} className="vf-pill">
                        <LayoutGrid className="size-4" />
                        {f}
                    </span>
                ))}
            </div>

            <div className="mt-5">
                <div className="text-sm font-semibold">Content pillars</div>
                <div className="mt-2 grid md:grid-cols-2 gap-3">
                    {p.contentPillars?.map((c) => (
                        <div key={c.name} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                            <div className="font-semibold">{c.name}</div>
                            <div className="mt-1 text-sm text-white/70">{c.angle}</div>
                            <div className="mt-3 space-y-1">
                                {c.examples?.slice(0, 3).map((ex) => (
                                    <div key={ex} className="text-xs text-white/70 flex gap-2">
                                        <span className="text-white/45">•</span>
                                        <span>{ex}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="mt-5 grid md:grid-cols-2 gap-3">
                <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <div className="flex items-center gap-2 font-semibold">
                        <Flame className="size-4 text-[var(--vf-orange)]" />
                        Hooks
                    </div>
                    <div className="mt-2 space-y-1">
                        {p.hooks?.slice(0, 6).map((h) => (
                            <div key={h} className="text-xs text-white/70 flex gap-2">
                                <span className="text-white/45">•</span>
                                <span>{h}</span>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <div className="flex items-center gap-2 font-semibold">
                        <CheckCircle2 className="size-4 text-emerald-400" />
                        CTAs
                    </div>
                    <div className="mt-2 space-y-1">
                        {p.ctaExamples?.slice(0, 6).map((cta) => (
                            <div key={cta} className="text-xs text-white/70 flex gap-2">
                                <span className="text-white/45">•</span>
                                <span>{cta}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}

export function MarketPlan({ plan }: Props) {
    const [tab, setTab] = useState<Tab>("platforms");

    const sortedPlatforms = useMemo(() => {
        const items = plan?.platformPlans ?? [];
        return [...items].sort((a, b) => (b.frequencyPerWeek ?? 0) - (a.frequencyPerWeek ?? 0));
    }, [plan]);

    if (!plan) {
        return (
            <div className="vf-card p-6">
                <div className="text-sm text-white/70">Your plan will appear here.</div>
                <div className="mt-2 text-xs text-white/55">
                    Generate a plan from the chat panel. We’ll render platform strategy, pillars, hooks, CTAs, and metrics.
                </div>
            </div>
        );
    }

    return (
        <div className="vf-card p-6 flex flex-col gap-5">
            <div>
                <div className="text-xs text-white/55">Summary</div>
                <div className="mt-1 text-base sm:text-lg text-white/85 leading-relaxed">{plan.summary}</div>
                <div className="mt-2 text-xs text-white/45">Generated at: {plan.generatedAt}</div>
            </div>

            <div className="flex flex-wrap gap-2">
                <TabButton
                    active={tab === "platforms"}
                    onClick={() => setTab("platforms")}
                    icon={<LayoutGrid className="size-4" />}
                    label="Platforms"
                />
                <TabButton
                    active={tab === "measurement"}
                    onClick={() => setTab("measurement")}
                    icon={<BarChart3 className="size-4" />}
                    label="Measurement"
                />
                <TabButton
                    active={tab === "assumptions"}
                    onClick={() => setTab("assumptions")}
                    icon={<Target className="size-4" />}
                    label="Assumptions"
                />
                <TabButton
                    active={tab === "confidence"}
                    onClick={() => setTab("confidence")}
                    icon={<LineChart className="size-4" />}
                    label="Confidence"
                />
            </div>

            {tab === "platforms" && (
                <div className="space-y-4">
                    {sortedPlatforms.map((p) => (
                        <PlatformCard key={String(p.platform)} p={p} />
                    ))}
                </div>
            )}

            {tab === "measurement" && (
                <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
                    <div className="flex items-center gap-2 font-semibold">
                        <BarChart3 className="size-4" />
                        Measurement
                    </div>
                    <div className="mt-3">
                        <div className="text-xs text-white/55">North star metric</div>
                        <div className="text-sm text-white/85 mt-1">{plan.measurement?.northStarMetric}</div>
                    </div>
                    <div className="mt-4">
                        <div className="text-xs text-white/55">KPIs</div>
                        <div className="mt-2 space-y-1">
                            {plan.measurement?.kpis?.map((k) => (
                                <div key={k} className="text-sm text-white/75 flex gap-2">
                                    <span className="text-white/45">•</span>
                                    <span>{k}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                    <div className="mt-4">
                        <div className="text-xs text-white/55">Reporting cadence</div>
                        <div className="text-sm text-white/75 mt-1">{plan.measurement?.reportingCadence}</div>
                    </div>
                </div>
            )}

            {tab === "assumptions" && (
                <div className="space-y-3">
                    {plan.assumptions?.map((a) => (
                        <div key={a.assumption} className="rounded-2xl border border-white/10 bg-white/5 p-5">
                            <div className="flex items-start justify-between gap-4">
                                <div className="font-semibold">{a.assumption}</div>
                                <span className="vf-pill">Risk: {a.riskLevel}</span>
                            </div>
                            <div className="mt-2 text-sm text-white/70">
                                <span className="text-white/55">How to test:</span> {a.howToTest}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {tab === "confidence" && (
                <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
                    <div className="flex items-center justify-between gap-4">
                        <div className="font-semibold">Confidence score</div>
                        <span className="vf-pill">{Math.round((plan.confidence?.score ?? 0) * 100)}%</span>
                    </div>
                    <div className="mt-3 space-y-1">
                        {plan.confidence?.reasons?.map((r) => (
                            <div key={r} className="text-sm text-white/75 flex gap-2">
                                <span className="text-white/45">•</span>
                                <span>{r}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
