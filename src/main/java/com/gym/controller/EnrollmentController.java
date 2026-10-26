package com.gym.controller;

import com.gym.model.dto.EnrollmentForm;
import com.gym.pattern.strategy.FeeStrategyFactory;
import com.gym.pattern.templatemethod.EnrollmentContext;
import com.gym.service.EnrollmentService;
import com.gym.service.MemberService;
import com.gym.service.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller strictly handling HTTP requests for Member Enrollment.
 * Delegates to EnrollmentService which executes Template Method workflow.
 */
@Controller
@RequestMapping("/enroll")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final FeeStrategyFactory feeStrategyFactory;

    public EnrollmentController(EnrollmentService enrollmentService,
                                MemberService memberService,
                                TrainerService trainerService,
                                FeeStrategyFactory feeStrategyFactory) {
        this.enrollmentService = enrollmentService;
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.feeStrategyFactory = feeStrategyFactory;
    }

    @GetMapping
    public String showEnrollForm(@RequestParam(value = "memberId", required = false) Long preselectedMemberId,
                                 Model model) {
        EnrollmentForm form = new EnrollmentForm();
        if (preselectedMemberId != null) {
            form.setMemberId(preselectedMemberId);
        }

        model.addAttribute("enrollmentForm", form);
        model.addAttribute("members", memberService.getAllMembers());
        model.addAttribute("trainers", trainerService.getAllTrainers());
        model.addAttribute("strategies", feeStrategyFactory.getAllStrategies());
        return "enroll";
    }

    @PostMapping("/submit")
    public String processEnrollment(@ModelAttribute("enrollmentForm") EnrollmentForm form,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (form.getMemberId() == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "Please select a member to enroll.");
                return "redirect:/enroll";
            }
            if (form.getPlanType() == null || form.getPlanType().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "Please select a valid membership plan.");
                return "redirect:/enroll";
            }

            EnrollmentContext resultContext = enrollmentService.executeEnrollment(form);
            redirectAttributes.addFlashAttribute("successMsg", resultContext.getWelcomeMessage());
            return "redirect:/plans/" + form.getMemberId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Enrollment Failed: " + e.getMessage());
            return "redirect:/enroll";
        }
    }
}
