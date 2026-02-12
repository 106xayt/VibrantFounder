package no.vibrantfounder.bachelor.marketing.memory;

import no.vibrantfounder.bachelor.marketing.domain.MarketingPlan;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory store for marketing plans.
 *
 * This is a temporary persistence mechanism used to support
 * development and testing without introducing a database.
 */
public class InMemoryPlanStore {

    private final Map<UUID, MarketingPlan> store = new ConcurrentHashMap<>();

    public UUID save(MarketingPlan plan) {
        UUID id = UUID.randomUUID();
        store.put(id, plan);
        return id;
    }

    public Optional<MarketingPlan> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public void clear() {
        store.clear();
    }
}
