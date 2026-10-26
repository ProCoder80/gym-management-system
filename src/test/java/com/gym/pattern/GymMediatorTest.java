package com.gym.pattern;

import com.gym.dao.ClassBookingDAO;
import com.gym.dao.GymClassDAO;
import com.gym.dao.MemberDAO;
import com.gym.dao.TrainerDAO;
import com.gym.model.entity.GymClass;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.model.entity.Trainer;
import com.gym.pattern.mediator.GymCoordinator;
import com.gym.pattern.mediator.MediatorResponse;
import com.gym.service.ClassScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PATTERN TEST: Mediator Pattern Routing & Centralized Business Rules
 * Verifies that GymCoordinator mediates bookings between Member, Trainer, and ClassSchedule,
 * enforcing rejections for full classes and unavailable trainers, and auto-enrolling default classes.
 */
@ExtendWith(MockitoExtension.class)
class GymMediatorTest {

    @Mock
    private MemberDAO memberDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private GymClassDAO gymClassDAO;
    @Mock
    private ClassBookingDAO classBookingDAO;

    private GymCoordinator mediator;
    private ClassScheduleService classScheduleService;

    @BeforeEach
    void setUp() {
        mediator = new GymCoordinator(memberDAO, trainerDAO, gymClassDAO, classBookingDAO);
        classScheduleService = new ClassScheduleService(gymClassDAO, classBookingDAO, mediator);
    }

    @Test
    @DisplayName("Mediator Rejection: Rejects booking if gym class is at full capacity")
    void testMediatorRejectionClassFull() {
        Long memberId = 1L;
        Long classId = 3L;

        Member member = new Member(memberId, "John", "john@example.com", "123", "ACTIVE", LocalDate.now());
        GymClass fullClass = new GymClass(classId, "High-Octane HIIT", "Desc", 3L, "Mon 5:30PM", 12, 12);

        when(memberDAO.findById(memberId)).thenReturn(Optional.of(member));
        when(gymClassDAO.findById(classId)).thenReturn(Optional.of(fullClass));

        // Attempt mediated booking through ClassScheduleService
        MediatorResponse response = classScheduleService.bookClass(memberId, classId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("maximum capacity"));
        verify(classBookingDAO, never()).create(any());
        verify(gymClassDAO, never()).incrementEnrolled(any());
    }

    @Test
    @DisplayName("Mediator Rejection: Rejects booking if assigned trainer is unavailable/off-duty")
    void testMediatorRejectionTrainerUnavailable() {
        Long memberId = 2L;
        Long classId = 1L;

        Member member = new Member(memberId, "Sarah", "sarah@example.com", "123", "ACTIVE", LocalDate.now());
        GymClass availableClass = new GymClass(classId, "Powerlifting", "Desc", 1L, "Tue 6PM", 10, 5);
        Trainer offDutyTrainer = new Trainer(1L, "Alex Turner", "Powerlifting", false, "contact");

        when(memberDAO.findById(memberId)).thenReturn(Optional.of(member));
        when(gymClassDAO.findById(classId)).thenReturn(Optional.of(availableClass));
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(offDutyTrainer));

        MediatorResponse response = classScheduleService.bookClass(memberId, classId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("currently unavailable or off-duty"));
        verify(classBookingDAO, never()).create(any());
    }

    @Test
    @DisplayName("Mediator Rejection: Rejects booking if member is already booked in the class")
    void testMediatorRejectionMemberAlreadyBooked() {
        Long memberId = 3L;
        Long classId = 2L;

        Member member = new Member(memberId, "Michael", "m@example.com", "123", "ACTIVE", LocalDate.now());
        GymClass openClass = new GymClass(classId, "Yoga Flow", "Desc", 2L, "Wed 7AM", 15, 8);
        Trainer availableTrainer = new Trainer(2L, "Maya Patel", "Yoga", true, "contact");

        when(memberDAO.findById(memberId)).thenReturn(Optional.of(member));
        when(gymClassDAO.findById(classId)).thenReturn(Optional.of(openClass));
        when(trainerDAO.findById(2L)).thenReturn(Optional.of(availableTrainer));
        when(classBookingDAO.isMemberBooked(memberId, classId)).thenReturn(true);

        MediatorResponse response = classScheduleService.bookClass(memberId, classId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("already booked in class"));
        verify(classBookingDAO, never()).create(any());
    }

    @Test
    @DisplayName("Mediator Success: Successfully confirms booking when constraints are satisfied")
    void testMediatorSuccessBooking() {
        Long memberId = 4L;
        Long classId = 4L;

        Member member = new Member(memberId, "Emma", "emma@example.com", "123", "ACTIVE", LocalDate.now());
        GymClass openClass = new GymClass(classId, "Cardio Blast", "Desc", 1L, "Sat 9AM", 20, 10);
        Trainer availableTrainer = new Trainer(1L, "Alex", "Fitness", true, "contact");

        when(memberDAO.findById(memberId)).thenReturn(Optional.of(member));
        when(gymClassDAO.findById(classId)).thenReturn(Optional.of(openClass));
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(availableTrainer));
        when(classBookingDAO.isMemberBooked(memberId, classId)).thenReturn(false);

        MediatorResponse response = classScheduleService.bookClass(memberId, classId);

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Booking confirmed"));
        verify(classBookingDAO).create(any());
        verify(gymClassDAO).incrementEnrolled(classId);
    }

    @Test
    @DisplayName("Mediator: Auto-registers default introductory class on enrollment notification")
    void testMediatorAutoRegisterOnEnrollment() {
        Long memberId = 5L;
        Membership membership = new Membership.Builder(memberId, "MONTHLY", LocalDate.now())
                .trainer(2L)
                .build();

        GymClass defaultClass = new GymClass(1L, "Sunrise Yoga Flow", "Intro", 2L, "Mon 7AM", 15, 5);
        Trainer trainer = new Trainer(2L, "Maya Patel", "Yoga", true, "contact");

        when(trainerDAO.findById(2L)).thenReturn(Optional.of(trainer));
        when(gymClassDAO.findDefaultAvailableClass()).thenReturn(Optional.of(defaultClass));
        when(classBookingDAO.isMemberBooked(memberId, 1L)).thenReturn(false);

        // Notify mediator of enrollment event
        mediator.notify(this, "MEMBER_ENROLLED", membership);

        verify(classBookingDAO).create(any());
        verify(gymClassDAO).incrementEnrolled(1L);
    }
}
