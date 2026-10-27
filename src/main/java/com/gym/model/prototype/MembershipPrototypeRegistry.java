package com.gym.model.prototype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PATTERN: Prototype Pattern (Prototype Registry / Manager)
 *
 * Intent:
 * Manages an in-memory catalog of predefined membership prototypes stored in a {@link Map}.
 * Clients retrieve prototypes by plan type or template key and obtain an independent CLONE
 * before applying custom member-specific parameters.
 *
 * Responsibilities:
 * - Bootstraps and registers standard templates (Basic, Premium, Student, Couple) once at startup.
 * - Managed as a Spring singleton bean (avoiding new registry instantiation on every HTTP request).
 * - Enforces rejection of unknown or invalid prototype keys with meaningful {@link IllegalArgumentException}.
 * - Dispenses independent deep clones via {@link #getClonedPrototype(String)} so mutations to a clone
 *   never pollute the master prototype or other clones.
 */
@Component
public class MembershipPrototypeRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MembershipPrototypeRegistry.class);

    /**
     * Map-based registry storing master prototypical instances.
     * Keyed by uppercase plan/template keys (e.g. "BASIC", "MONTHLY", "PREMIUM", "YEARLY", "STUDENT", "COUPLE").
     */
    private final Map<String, MembershipPrototype> prototypeRegistry = new ConcurrentHashMap<>();

    public MembershipPrototypeRegistry() {
        initializeDefaultPrototypes();
    }

    /**
     * Registers default prototypical instances once during application startup.
     * Supports both template names and plan type aliases for robust lookup.
     */
    public void initializeDefaultPrototypes() {
        logger.info("Initializing Membership Prototype Registry templates at application startup...");

        // 1. Basic Plan (Monthly)
        BasicMembershipPrototype basic = new BasicMembershipPrototype();
        registerPrototype("BASIC", basic);
        registerPrototype("MONTHLY", basic);

        // 2. Premium Plan (Yearly)
        PremiumMembershipPrototype premium = new PremiumMembershipPrototype();
        registerPrototype("PREMIUM", premium);
        registerPrototype("YEARLY", premium);

        // 3. Student Plan
        StudentMembershipPrototype student = new StudentMembershipPrototype();
        registerPrototype("STUDENT", student);

        // 4. Couple Plan
        CoupleMembershipPrototype couple = new CoupleMembershipPrototype();
        registerPrototype("COUPLE", couple);

        logger.info("Membership Prototype Registry successfully loaded {} template mappings: {}",
                prototypeRegistry.size(), prototypeRegistry.keySet());
    }

    /**
     * Registers a prototype into the internal map.
     *
     * @param key       the lookup key (case-insensitive)
     * @param prototype the prototype instance
     * @throws IllegalArgumentException if key or prototype is null/empty
     */
    public void registerPrototype(String key, MembershipPrototype prototype) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Prototype key cannot be null or blank.");
        }
        if (prototype == null) {
            throw new IllegalArgumentException("Prototype instance cannot be null.");
        }
        prototypeRegistry.put(key.trim().toUpperCase(Locale.ROOT), prototype);
    }

    /**
     * Checks if a prototype is registered under the given key.
     *
     * @param key template or plan type key
     * @return true if key exists, false otherwise
     */
    public boolean containsPrototype(String key) {
        if (key == null) return false;
        return prototypeRegistry.containsKey(key.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * Retrieves the master prototype registered under the given key.
     *
     * @param key the template name or plan type key
     * @return the master prototype instance
     * @throws IllegalArgumentException if the key is unknown or blank
     */
    public MembershipPrototype getPrototype(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Membership template key cannot be null or empty.");
        }

        String normalizedKey = key.trim().toUpperCase(Locale.ROOT);
        MembershipPrototype prototype = prototypeRegistry.get(normalizedKey);

        if (prototype == null) {
            throw new IllegalArgumentException(String.format(
                    "Unknown membership prototype template key: '%s'. Available registered prototypes: %s",
                    key, prototypeRegistry.keySet()
            ));
        }

        return prototype;
    }

    /**
     * Retrieves a prototype by key and returns an INDEPENDENT DEEP CLONE.
     * Guaranteed to return a fresh copy whose state can be customized freely
     * without modifying the master template or any other clones.
     *
     * @param key the template name or plan type key
     * @return an independent clone of the requested prototype
     * @throws IllegalArgumentException if the key is unknown
     */
    public MembershipPrototype getClonedPrototype(String key) {
        MembershipPrototype masterPrototype = getPrototype(key);
        return masterPrototype.clonePrototype();
    }

    /**
     * Returns an unmodifiable view of the master prototypes for UI template selectors.
     * Returns distinct templates by template name (Basic, Premium, Student, Couple).
     */
    public Map<String, MembershipPrototype> getAllPrototypes() {
        Map<String, MembershipPrototype> distinctTemplates = new LinkedHashMap<>();
        distinctTemplates.put("BASIC", prototypeRegistry.get("BASIC"));
        distinctTemplates.put("PREMIUM", prototypeRegistry.get("PREMIUM"));
        distinctTemplates.put("STUDENT", prototypeRegistry.get("STUDENT"));
        distinctTemplates.put("COUPLE", prototypeRegistry.get("COUPLE"));
        return Collections.unmodifiableMap(distinctTemplates);
    }

    /**
     * Returns the total number of registered lookup keys.
     */
    public int size() {
        return prototypeRegistry.size();
    }
}
