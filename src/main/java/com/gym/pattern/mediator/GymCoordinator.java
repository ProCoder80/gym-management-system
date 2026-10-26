package com.gym.pattern.mediator;

import com.gym.dao.ClassBookingDAO;
import com.gym.dao.GymClassDAO;
import com.gym.dao.MemberDAO;
import com.gym.dao.TrainerDAO;
import com.gym.model.entity.ClassBooking;
import com.gym.model.entity.GymClass;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.model.entity.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PATTERN: Mediator Pattern (Concrete Mediator)
 * 
 * Intent:
 * GymCoordinator coordinates all inter-service communications between:
 * - MemberService
 * - TrainerService
 * - ClassScheduleService
 * 
 * Crucial Rule:
 * The colleague services NEVER talk directly to one another.
 * They notify GymCoordinator, which implements the complex cross-service rules:
 * 1. On enrollment: Assign trainer, auto-register default class.
 * 2. On booking: Reject booking if trainer is unavailable or class is full.
 */
@Component
public class GymCoordinator implements GymMediator {

    private static final Logger logger = LoggerFactory.getLogger(GymCoordinator.class);

    private final MemberDAO memberDAO;
    private final TrainerDAO trainerDAO;
    private final GymClassDAO gymClassDAO;
    private final ClassBookingDAO classBookingDAO;

    public GymCoordinator(MemberDAO memberDAO,
                          TrainerDAO trainerDAO,
                          GymClassDAO gymClassDAO,
                          ClassBookingDAO classBookingDAO) {
        this.memberDAO = memberDAO;
        this.trainerDAO = trainerDAO;
        this.gymClassDAO = gymClassDAO;
        this.classBookingDAO = classBookingDAO;
    }

    @Override
    public void notify(Object sender, String event, Object data) {
        logger.info("GymCoordinator received event: '{}' from sender: {}", event, sender.getClass().getSimpleName());

        if ("MEMBER_ENROLLED".equals(event) || "MEMBER_RENEWED".equals(event)) {
            if (data instanceof Membership membership) {
                processEnrollment(membership);
            }
        } else if ("TRAINER_STATUS_CHANGED".equals(event)) {
            logger.info("Trainer status change acknowledged by mediator.");
        }
    }

    /**
     * Post-enrollment mediator routine:
     * 1. Assign trainer and verify availability.
     * 2. Auto-register member into a default available introductory class.
     */
    @Override
    public MediatorResponse processEnrollment(Membership membership) {
        if (membership == null) {
            return MediatorResponse.failure("Enrollment coordination failed: Membership is null.");
        }

        Long memberId = membership.getMemberId();
        logger.info("Mediator processing post-enrollment routines for Member ID: {}", memberId);

        // Sub-step 1: Check Trainer assignment
        if (membership.getTrainerId() != null) {
            Optional<Trainer> trainerOpt = trainerDAO.findById(membership.getTrainerId());
            if (trainerOpt.isPresent()) {
                Trainer trainer = trainerOpt.get();
                logger.info("Assigned personal trainer: {} (Availability: {})", trainer.getName(), trainer.isAvailable());
            }
        }

        // Sub-step 2: Auto-register default introductory class
        Optional<GymClass> defaultClassOpt = gymClassDAO.findDefaultAvailableClass();
        if (defaultClassOpt.isPresent()) {
            GymClass defaultClass = defaultClassOpt.get();
            if (!classBookingDAO.isMemberBooked(memberId, defaultClass.getId())) {
                ClassBooking autoBooking = new ClassBooking(
                        null,
                        defaultClass.getId(),
                        memberId,
                        LocalDateTime.now(),
                        "CONFIRMED"
                );
                classBookingDAO.create(autoBooking);
                gymClassDAO.incrementEnrolled(defaultClass.getId());
                logger.info("Auto-registered Member ID: {} into default class: '{}'", memberId, defaultClass.getName());
                return MediatorResponse.success("Enrollment processed: Assigned trainer and registered for default class: " + defaultClass.getName());
            }
        }

        return MediatorResponse.success("Enrollment processed successfully.");
    }

    /**
     * Coordinates Class Booking:
     * - Reject booking if trainer is unavailable or class is full.
     * - Reject if member is already booked.
     */
    @Override
    public MediatorResponse bookClass(Long memberId, Long classId) {
        logger.info("GymCoordinator mediating class booking: Member ID {} -> Class ID {}", memberId, classId);

        // 1. Verify Member exists
        Optional<Member> memberOpt = memberDAO.findById(memberId);
        if (memberOpt.isEmpty()) {
            return MediatorResponse.failure("Booking rejected: Member with ID " + memberId + " not found.");
        }

        // 2. Verify Class exists
        Optional<GymClass> classOpt = gymClassDAO.findById(classId);
        if (classOpt.isEmpty()) {
            return MediatorResponse.failure("Booking rejected: Gym Class with ID " + classId + " not found.");
        }
        GymClass gymClass = classOpt.get();

        // 3. MEDIATOR REJECTION RULE 1: Reject if class is full
        if (gymClass.isFull()) {
            String rejectMsg = String.format("Booking rejected by Mediator: Class '%s' is at maximum capacity (%d/%d spots filled).",
                    gymClass.getName(), gymClass.getCurrentEnrolled(), gymClass.getCapacity());
            logger.warn(rejectMsg);
            return MediatorResponse.failure(rejectMsg);
        }

        // 4. MEDIATOR REJECTION RULE 2: Reject if assigned trainer is unavailable/off-duty
        Optional<Trainer> trainerOpt = trainerDAO.findById(gymClass.getTrainerId());
        if (trainerOpt.isPresent() && !trainerOpt.get().isAvailable()) {
            String rejectMsg = String.format("Booking rejected by Mediator: Trainer '%s' assigned to class '%s' is currently unavailable or off-duty.",
                    trainerOpt.get().getName(), gymClass.getName());
            logger.warn(rejectMsg);
            return MediatorResponse.failure(rejectMsg);
        }

        // 5. MEDIATOR REJECTION RULE 3: Reject if member already registered
        if (classBookingDAO.isMemberBooked(memberId, classId)) {
            String rejectMsg = String.format("Booking rejected by Mediator: Member '%s' is already booked in class '%s'.",
                    memberOpt.get().getName(), gymClass.getName());
            logger.warn(rejectMsg);
            return MediatorResponse.failure(rejectMsg);
        }

        // 6. Valid booking: Create booking & increment enrolled count
        ClassBooking booking = new ClassBooking(
                null,
                classId,
                memberId,
                LocalDateTime.now(),
                "CONFIRMED"
        );
        classBookingDAO.create(booking);
        gymClassDAO.incrementEnrolled(classId);

        String successMsg = String.format("Booking confirmed! '%s' has reserved a spot in '%s' (%s).",
                memberOpt.get().getName(), gymClass.getName(), gymClass.getScheduleTime());
        logger.info(successMsg);
        return MediatorResponse.success(successMsg, booking);
    }
}
