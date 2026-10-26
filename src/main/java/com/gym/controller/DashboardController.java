package com.gym.controller;

import com.gym.dao.MembershipDAO;
import com.gym.service.ClassScheduleService;
import com.gym.service.MemberService;
import com.gym.service.PaymentService;
import com.gym.service.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller strictly handling HTTP requests for the Gym Dashboard.
 * Business logic delegates entirely to services.
 */
@Controller
public class DashboardController {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final ClassScheduleService classScheduleService;
    private final PaymentService paymentService;
    private final MembershipDAO membershipDAO;

    public DashboardController(MemberService memberService,
                               TrainerService trainerService,
                               ClassScheduleService classScheduleService,
                               PaymentService paymentService,
                               MembershipDAO membershipDAO) {
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.classScheduleService = classScheduleService;
        this.paymentService = paymentService;
        this.membershipDAO = membershipDAO;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("totalMembers", memberService.countMembers());
        model.addAttribute("activeMemberships", membershipDAO.countActive());
        model.addAttribute("totalTrainers", trainerService.countTrainers());
        model.addAttribute("totalClasses", classScheduleService.countClasses());
        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        model.addAttribute("recentPayments", paymentService.getAllPayments().stream().limit(5).toList());
        model.addAttribute("classes", classScheduleService.getAllClasses());
        model.addAttribute("members", memberService.getAllMembers().stream().limit(6).toList());
        return "dashboard";
    }
}
