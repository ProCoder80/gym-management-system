package com.gym.service;

import com.gym.dao.TrainerDAO;
import com.gym.model.entity.Trainer;
import com.gym.pattern.mediator.GymMediator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * TrainerService Colleague in Mediator Pattern.
 * STRICT RULE: Does NOT reference MemberService or ClassScheduleService directly.
 * All cross-system notifications and coordinations occur via GymMediator.
 */
@Service
public class TrainerService {

    private final TrainerDAO trainerDAO;
    private final GymMediator gymMediator;

    public TrainerService(TrainerDAO trainerDAO, GymMediator gymMediator) {
        this.trainerDAO = trainerDAO;
        this.gymMediator = gymMediator;
    }

    public List<Trainer> getAllTrainers() {
        return trainerDAO.findAll();
    }

    public Optional<Trainer> getTrainerById(Long id) {
        return trainerDAO.findById(id);
    }

    public boolean toggleAvailability(Long id) {
        Optional<Trainer> opt = trainerDAO.findById(id);
        if (opt.isPresent()) {
            boolean newStatus = !opt.get().isAvailable();
            boolean updated = trainerDAO.updateAvailability(id, newStatus);
            if (updated) {
                // Inform mediator so class bookings can respond accordingly
                gymMediator.notify(this, "TRAINER_STATUS_CHANGED", id);
            }
            return updated;
        }
        return false;
    }

    public int countTrainers() {
        return trainerDAO.count();
    }
}
