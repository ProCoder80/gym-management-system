package com.gym.service;

import com.gym.dao.ClassBookingDAO;
import com.gym.dao.GymClassDAO;
import com.gym.model.entity.ClassBooking;
import com.gym.model.entity.GymClass;
import com.gym.pattern.mediator.GymMediator;
import com.gym.pattern.mediator.MediatorResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ClassScheduleService Colleague in Mediator Pattern.
 * STRICT RULE: Does NOT reference MemberService or TrainerService directly.
 * All class bookings are mediated strictly through GymMediator.
 */
@Service
public class ClassScheduleService {

    private final GymClassDAO gymClassDAO;
    private final ClassBookingDAO classBookingDAO;
    private final GymMediator gymMediator;

    public ClassScheduleService(GymClassDAO gymClassDAO,
                                ClassBookingDAO classBookingDAO,
                                GymMediator gymMediator) {
        this.gymClassDAO = gymClassDAO;
        this.classBookingDAO = classBookingDAO;
        this.gymMediator = gymMediator;
    }

    public List<GymClass> getAllClasses() {
        return gymClassDAO.findAll();
    }

    public Optional<GymClass> getClassById(Long id) {
        return gymClassDAO.findById(id);
    }

    public List<ClassBooking> getAllBookings() {
        return classBookingDAO.findAll();
    }

    public List<ClassBooking> getBookingsForMember(Long memberId) {
        return classBookingDAO.findByMemberId(memberId);
    }

    /**
     * Books a class for a member by delegating directly to the GymMediator.
     * The mediator enforces capacity and trainer availability constraints.
     */
    public MediatorResponse bookClass(Long memberId, Long classId) {
        return gymMediator.bookClass(memberId, classId);
    }

    public int countClasses() {
        return gymClassDAO.count();
    }
}
