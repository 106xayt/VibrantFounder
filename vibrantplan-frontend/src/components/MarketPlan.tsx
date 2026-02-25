import { useMemo, useState } from "react";
import type { MarketingPlanResponse } from "../types";
import { adaptPlanToVM } from "../utils/planAdapter";

type Tab = "overview" | "content";

interface Props {
    plan: MarketingPlanResponse | null;
}

function StatCard(props: { title: string; value: string; subtitle: string }) {
    return (
        <div className="vf-card p-5">
            <div className="text-sm font-semibold text-slate-700">{props.title}</div>
            <div className="mt-6 text-4xl font-extrabold">{props.value}</div>
            <div className="text-xs text-slate-500 mt-1">{props.subtitle}</div>
        </div>
    );
}

function SegmentedTabs({ tab, setTab }: { tab: Tab; setTab: (t: Tab) => void }) {
    return (
        <div className="vf-card p-2 relative overflow-hidden">
            {/* slider */}
            <div
                className={`absolute top-2 bottom-2 w-[calc(50%-8px)] rounded-xl vf-gradient transition-transform duration-300 ${
                    tab === "overview" ? "translate-x-0 left-2" : "translate-x-full left-0"
                }`}
                style={{
                    transform: tab === "overview" ? "translateX(0)" : "translateX(calc(100% + 8px))",
                }}
            />
            <div className="relative grid grid-cols-2 gap-2">
                <button
                    className={`rounded-xl py-3 font-semibold transition-colors ${
                        tab === "overview" ? "text-white" : "text-slate-700"
                    }`}
                    onClick={() => setTab("overview")}
                >
                    Overview
                </button>
                <button
                    className={`rounded-xl py-3 font-semibold transition-colors ${
                        tab === "content" ? "text-white" : "text-slate-700"
                    }`}
                    onClick={() => setTab("content")}
                >
                    Content Activity
                </button>
            </div>
        </div>
    );
}

export function MarketPlan({ plan }: Props) {
    const [tab, setTab] = useState<Tab>("overview");
    const vm = useMemo(() => (plan ? adaptPlanToVM(plan) : null), [plan]);

    if (!plan || !vm) return null;

    const todayTasksValue =
        vm.stats.todayTasks == null ? "—" : String(vm.stats.todayTasks);

    const growthValue =
        vm.stats.growthPotential == null ? "—" : vm.stats.growthPotential;

    const planPeriodValue =
        vm.stats.planPeriodWeeks == null ? "—" : String(vm.stats.planPeriodWeeks);

    const progressValue =
        vm.stats.goalProgressPct == null ? "—" : `${vm.stats.goalProgressPct}%`;

    return (
        <div className="space-y-6">
            {/* Title */}
            <div className="text-center">
                <div className="inline-flex items-center gap-2 vf-badge">
                    ✨ Step 2: Review Your Plan
                </div>
                <h1 className="text-5xl font-extrabold tracking-tight mt-4">Market Plan</h1>
                <p className="text-slate-500 mt-2">
                    Complete marketing strategy tailored for your business
                </p>
            </div>

            {/* Stats */}
            <div className="grid md:grid-cols-4 gap-4">
                <StatCard
                    title="Today's Tasks"
                    value={todayTasksValue}
                    subtitle={
                        vm.stats.todayTasks == null
                            ? "Not provided by backend yet"
                            : "Tasks scheduled for today"
                    }
                />
                <StatCard
                    title="Growth Potential"
                    value={growthValue}
                    subtitle={
                        vm.stats.growthPotential == null
                            ? "Not provided by backend yet"
                            : "Based on AI confidence/assessment"
                    }
                />
                <StatCard
                    title="Plan Period"
                    value={planPeriodValue}
                    subtitle={
                        vm.stats.planPeriodWeeks == null
                            ? "Not provided by backend yet"
                            : "Marketing plan duration (weeks)"
                    }
                />
                <StatCard
                    title="Goal Progress"
                    value={progressValue}
                    subtitle={
                        vm.stats.goalProgressPct == null
                            ? "Tracking not implemented yet"
                            : "Overall achievements"
                    }
                />
            </div>

            {/* Metrics accordion header */}
            <div className="vf-card p-5 flex items-center justify-between">
                <div>
                    <div className="font-semibold">Platform Goals & Metrics</div>
                    <div className="text-sm text-slate-500">
                        Uses backend metrics when available (rawJson.metrics)
                    </div>
                </div>
                <div className="text-slate-400">⌄</div>
            </div>

            {/* Better toggle */}
            <SegmentedTabs tab={tab} setTab={setTab} />

            {/* OVERVIEW */}
            {tab === "overview" && (
                <div className="vf-card p-6">
                    <div className="text-xl font-extrabold mb-2">Platform Strategy</div>
                    {vm.summary ? (
                        <div className="text-sm text-slate-600 mb-6">{vm.summary}</div>
                    ) : (
                        <div className="text-sm text-slate-500 mb-6">
                            (No summary provided by backend in rawJson)
                        </div>
                    )}

                    <div className="grid md:grid-cols-2 gap-4">
                        {vm.platforms.map((p, i) => (
                            <div key={i} className="rounded-2xl border border-black/10 bg-white/70 p-5">
                                <div className="flex items-start justify-between gap-3">
                                    <div>
                                        <div className="font-semibold">{p.platform}</div>
                                        <div className="text-xs text-slate-500 mt-1">
                                            {p.frequencyPerWeek}× / week
                                        </div>
                                    </div>
                                    <span className="vf-badge">{p.frequencyPerWeek}×</span>
                                </div>

                                {p.rationale && (
                                    <p className="text-sm text-slate-700 mt-3 leading-relaxed">{p.rationale}</p>
                                )}

                                {/* Only show if backend/rawJson includes them */}
                                {!!p.pillars?.length && (
                                    <div className="mt-4">
                                        <div className="text-xs font-bold text-slate-500 uppercase tracking-wide mb-2">
                                            Content Pillars
                                        </div>
                                        <div className="space-y-2">
                                            {p.pillars.slice(0, 3).map((c, idx) => (
                                                <div key={idx} className="rounded-xl border border-black/10 bg-white p-3">
                                                    <div className="font-semibold text-sm">{c.name}</div>
                                                    {c.angle && <div className="text-xs text-slate-600 mt-1">{c.angle}</div>}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {!!p.hooks?.length && (
                                    <div className="mt-4">
                                        <div className="text-xs font-bold text-slate-500 uppercase tracking-wide mb-2">Hooks</div>
                                        <ul className="text-sm text-slate-700 space-y-1">
                                            {p.hooks.slice(0, 3).map((h, idx) => (
                                                <li key={idx}>• {h}</li>
                                            ))}
                                        </ul>
                                    </div>
                                )}

                                {!!p.ctas?.length && (
                                    <div className="mt-4">
                                        <div className="text-xs font-bold text-slate-500 uppercase tracking-wide mb-2">CTAs</div>
                                        <ul className="text-sm text-slate-700 space-y-1">
                                            {p.ctas.slice(0, 3).map((c, idx) => (
                                                <li key={idx}>• {c}</li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* CONTENT ACTIVITY */}
            {tab === "content" && (
                <div className="vf-card p-6">
                    <div className="text-xl font-extrabold mb-2">Content Activity</div>

                    {!vm.calendar && !vm.contentIdeas && (
                        <div className="text-sm text-slate-600">
                            Backend doesn’t provide calendar/tasks/contentIdeas yet.
                            <br />
                            ✅ Next step is to include these in the AI JSON contract (rawJson) or add endpoints.
                        </div>
                    )}

                    {vm.contentIdeas && (
                        <div className="mt-5">
                            <div className="text-lg font-bold mb-3">Content Ideas</div>
                            <div className="grid md:grid-cols-3 gap-4">
                                {vm.contentIdeas.map((c, i) => (
                                    <div key={c.id ?? i} className="rounded-2xl border border-black/10 bg-white/70 p-4">
                                        <div className="text-xs font-semibold text-slate-500">{c.type || "Idea"}</div>
                                        <div className="mt-2 font-extrabold">{c.title}</div>
                                        {c.description && <div className="text-sm text-slate-600 mt-2">{c.description}</div>}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {vm.calendar && (
                        <div className="mt-6">
                            <div className="text-lg font-bold mb-2">Calendar</div>
                            <div className="text-sm text-slate-500">
                                (Rendered from backend calendar in rawJson)
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* Debug */}
            {plan.rawJson && (
                <details className="vf-card p-5">
                    <summary className="cursor-pointer text-sm font-extrabold text-slate-700">
                        View raw AI JSON
                    </summary>
                    <pre className="mt-3 max-h-72 overflow-auto rounded-xl bg-slate-950 text-slate-100 p-4 text-[11px] leading-relaxed">
            {plan.rawJson}
          </pre>
                </details>
            )}
        </div>
    );
}