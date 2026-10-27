package com.gym.pattern.prototype;

/**
 * PATTERN: Prototype Pattern (Package Aliasing Interface)
 *
 * Extends {@link com.gym.model.prototype.MembershipPrototype} to ensure full
 * backwards-compatibility and convention consistency across both package hierarchies:
 * - {@code com.gym.model.prototype}
 * - {@code com.gym.pattern.prototype}
 */
public interface MembershipPrototype extends com.gym.model.prototype.MembershipPrototype {
}
