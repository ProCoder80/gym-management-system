package com.gym.controller;

import com.gym.model.dto.ClassBookingForm;
import com.gym.pattern.mediator.MediatorResponse;
import com.gym.service.ClassScheduleService;
import com.gym.service.MemberService;
import com.gym.service.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller strictly handling HTTP requests for Class Booking and schedules.
 * All booking requests are mediated strictly through the Mediator pattern.
 */
@Controller
@RequestMapping("/classes")
public class ClassController {

    private final ClassScheduleService classScheduleService;
    private final MemberService memberService;
    private final TrainerService trainerService;

    public ClassController(ClassScheduleService classScheduleService,
                           MemberService memberService,
                           TrainerService trainerService) {
        this.classScheduleService = classScheduleService;
        this.memberService = memberService;
        this.trainerService = trainerService;
    }

    @GetMapping
    public String listClasses(Model model) {
        model.addAttribute("classes", classScheduleService.getAllClasses());
        model.addAttribute("members", memberService.getAllMembers());
        model.addAttribute("trainers", trainerService.getAllTrainers());
        model.addAttribute("bookings", classScheduleService.getAllBookings());
        model.addAttribute("bookingForm", new ClassBookingForm());
        return "classes";
    }

    @PostMapping("/book")
    public String bookClass(@ModelAttribute("bookingForm") ClassBookingForm form,
                            RedirectAttributes redirectAttributes) {
        if (form.getMemberId() == null || form.getClassId() == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please select both a member and a gym class.");
            return "redirect:/classes";
        }

        // PATTERN: Delegating booking request strictly to Mediator
        MediatorResponse response = classScheduleService.bookClass(form.getMemberId(), form.getClassId());

        if (response.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMsg", response.getMessage());
        } else {
            // Displays Mediator rejection message (e.g. class full or trainer unavailable)
            redirectAttributes.addFlashAttribute("errorMsg", response.getMessage());
        }
        return "redirect:/classes";
    }

    @PostMapping("/trainers/toggle/{trainerId}")
    public String toggleTrainerStatus(@PathVariable Long trainerId,
                                      RedirectAttributes redirectAttributes) {
        boolean toggled = trainerService.toggleAvailability(trainerId);
        if (toggled) {
            redirectAttributes.addFlashAttribute("successMsg", "Trainer availability status toggled successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Could not toggle trainer status.");
        }
        return "redirect:/classes";
    }
}
