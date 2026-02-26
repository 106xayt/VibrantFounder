import { useState } from "react";
import { Sparkles } from "lucide-react";
import { AiChat } from "./components/AiChat";
import { MarketPlan } from "./components/MarketPlan";
import type { MarketingPlanResponse } from "./types";

export default function App() {
    const [plan, setPlan] = useState<MarketingPlanResponse | null>(null);

    return (
        <div className="min-h-screen">
            <header className="sticky top-0 z-10 border-b border-white/10 bg-black/30 backdrop-blur">
                <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="size-9 rounded-2xl bg-gradient-to-br from-[var(--vf-orange)] to-[var(--vf-blue)] flex items-center justify-center shadow-lg">
                            <Sparkles className="size-5 text-white" />
                        </div>
                        <div>
                            <h1 className="font-semibold tracking-tight text-base leading-none">
                                VibrantFounder
                            </h1>
                            <p className="text-[11px] text-white/60">
                                Your AI marketing co-founder
                            </p>
                        </div>
                    </div>

                    <div className="hidden sm:flex items-center gap-2">
                        <span className="vf-pill">Spring Boot</span>
                        <span className="vf-pill">Claude</span>
                        <span className="vf-pill">JSON repair</span>
                    </div>
                </div>
            </header>

            <main className="max-w-6xl mx-auto px-6 py-10">
                <div className="mb-8">
                    <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight">
                        Generate a plan you can execute.
                    </h2>
                    <p className="mt-2 text-white/60 max-w-2xl">
                        Define your industry, audience, goals, channels, and weekly output. We’ll return a structured plan:
                        platforms, content pillars, hooks, CTAs, assumptions, and measurement.
                    </p>
                </div>

                <div className="grid lg:grid-cols-[420px_1fr] gap-6">
                    <AiChat onPlanGenerated={setPlan} />
                    <MarketPlan plan={plan} />
                </div>

                <div className="mt-10 text-xs text-white/45">
                    Pro tip: If the model returns malformed JSON, the backend repair prompt fixes and retries automatically.
                </div>
            </main>
        </div>
    );
}