package com.gym.pattern.mediator;

import com.gym.model.entity.Membership;

/**
 * PATTERN: Mediator Pattern
 * 
 * Intent:
 * Defines an object that encapsulates how a set of objects interact.
 * Mediator promotes loose coupling by keeping objects from referring to each other
 * explicitly, and it lets you vary their interaction independently.
 * 
 * Colleague Components:
 * - MemberService
 * - TrainerService
 * - ClassScheduleService
 * 
 * Strict Constraint:
 * The three colleague services NEVER reference each other directly.
 * Instead, they interact entirely through calls to:
 * mediator.notify(this, event, data) and mediator-coordinated business actions.
 */
public interface GymMediator {

    /**
     * General notification method dispatched by colleagues.
     *
     * @param sender The colleague initiating the event
     * @param event  Event name (e.g. "MEMBER_ENROLLED", "TRAINER_STATUS_CHANGED")
     * @param data   Optional event data (e.g. Membership, Trainer ID, etc.)
     */
    void notify(Object sender, String event, Object data);

    /**
     * Coordinates booking a gym class between MemberService, TrainerService, and ClassScheduleService.
     * Centralizes the validation rules:
     * - Rejects booking if class is full (currentEnrolled >= capacity)
     * - Rejects booking if assigned trainer is unavailable/off-duty
     * - Rejects booking if member is already registered
     *
     * @param memberId ID of the member requesting the booking
     * @param classId  ID of the gym class
     * @return MediatorResponse with success or rejection message
     */
    MediatorResponse bookClass(Long memberId, Long classId);

    /**
     * Coordinates post-enrollment logic:
     * - Assigns designated trainer (if specified) and verifies availability
     * - Auto-registers member into a default introductory class
     *
     * @param membership The newly activated membership
     * @return MediatorResponse
     */
    MediatorResponse processEnrollment(Membership membership);
}
