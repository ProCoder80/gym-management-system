package com.gym.pattern.strategy;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PATTERN: Strategy Pattern Factory (Map-Based Registry)
 * 
 * Intent:
 * Replaces conditional branching (if/else or switch-case statements) with an O(1)
 * hash-map lookup. Strategies register themselves by name and plan alias.
 * 
 * Conforms strictly to:
 * "chosen via a Map/factory, no if/else."
 */
@Component
public class FeeStrategyFactory {

    private final Map<String, FeeStrategy> strategyRegistry = new HashMap<>();

    public FeeStrategyFactory(List<FeeStrategy> strategies) {
        for (FeeStrategy strategy : strategies) {
            // Register strategy by its class/bean name
            registerStrategy(strategy.getStrategyName(), strategy);

            // Also register common aliases (e.g. "MONTHLY", "YEARLY", "STUDENT", "COUPLE")
            String cleanName = strategy.getStrategyName().replace("Fee", "").toUpperCase(Locale.ROOT);
            registerStrategy(cleanName, strategy);
        }
    }

    /**
     * Registers a strategy into the map registry.
     */
    public void registerStrategy(String key, FeeStrategy strategy) {
        if (key != null && strategy != null) {
            strategyRegistry.put(key.toUpperCase(Locale.ROOT), strategy);
        }
    }

    /**
     * Resolves the requested FeeStrategy purely via Map lookup.
     * ZERO if/else branching!
     *
     * @param planOrStrategyName The strategy name or plan type (e.g. "MonthlyFee", "MONTHLY", "YEARLY")
     * @return The matched FeeStrategy instance
     * @throws IllegalArgumentException if the key does not map to any strategy
     */
    public FeeStrategy getStrategy(String planOrStrategyName) {
        if (planOrStrategyName == null || planOrStrategyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Strategy key cannot be null or empty.");
        }

        String lookupKey = planOrStrategyName.trim().toUpperCase(Locale.ROOT);
        FeeStrategy strategy = strategyRegistry.get(lookupKey);

        // Fallback check if "FEE" suffix wasn't provided or vice versa
        if (strategy == null && !lookupKey.endsWith("FEE")) {
            strategy = strategyRegistry.get(lookupKey + "FEE");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("No fee calculation strategy registered for key: '" + planOrStrategyName +
                    "'. Registered strategies: " + strategyRegistry.keySet());
        }

        return strategy;
    }

    /**
     * Returns an unmodifiable map of all registered strategies.
     */
    public Map<String, FeeStrategy> getAllStrategies() {
        return Collections.unmodifiableMap(strategyRegistry);
    }
}
