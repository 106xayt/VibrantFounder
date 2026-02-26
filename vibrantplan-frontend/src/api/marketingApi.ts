import type { GeneratePlanRequest, MarketingPlanResponse } from "../types";

const API_BASE = ""; // bruker Vite proxy (/api -> backend)

function buildHeaders() {
    const headers: Record<string, string> = {
        "Content-Type": "application/json",
    };

    const user = import.meta.env.VITE_API_USER;
    const pass = import.meta.env.VITE_API_PASS;
    if (user && pass) {
        headers["Authorization"] = `Basic ${btoa(`${user}:${pass}`)}`;
    }

    return headers;
}

export async function generateMarketingPlan(
    request: GeneratePlanRequest
): Promise<MarketingPlanResponse> {
    const res = await fetch(`${API_BASE}/api/v1/marketing/plan`, {
        method: "POST",
        headers: buildHeaders(),
        body: JSON.stringify(request),
    });

    const text = await res.text();

    if (!res.ok) {
        let message = `Request failed (${res.status})`;
        try {
            const err = JSON.parse(text);
            if (err?.message) message = err.message;
        } catch {
            if (text) message = text;
        }
        throw new Error(message);
    }

    return JSON.parse(text) as MarketingPlanResponse;
}