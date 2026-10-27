package com.gym.controller;

import com.gym.model.dto.EnrollmentForm;
import com.gym.model.prototype.MembershipPrototype;
import com.gym.model.prototype.MembershipPrototypeRegistry;
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
 * Integrates Prototype Pattern templates to prepopulate defaults,
 * then delegates to EnrollmentService for Template Method / Builder execution.
 */
@Controller
@RequestMapping("/enroll")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final FeeStrategyFactory feeStrategyFactory;
    private final MembershipPrototypeRegistry prototypeRegistry;

    public EnrollmentController(EnrollmentService enrollmentService,
                                MemberService memberService,
                                TrainerService trainerService,
                                FeeStrategyFactory feeStrategyFactory,
                                MembershipPrototypeRegistry prototypeRegistry) {
        this.enrollmentService = enrollmentService;
        this.memberService = memberService;
        this.trainerService = trainerService;
        this.feeStrategyFactory = feeStrategyFactory;
        this.prototypeRegistry = prototypeRegistry;
    }

    @GetMapping
    public String showEnrollForm(@RequestParam(value = "memberId", required = false) Long preselectedMemberId,
                                 @RequestParam(value = "template", required = false, defaultValue = "BASIC") String templateKey,
                                 Model model) {
        EnrollmentForm form = new EnrollmentForm();
        if (preselectedMemberId != null) {
            form.setMemberId(preselectedMemberId);
        }

        // PATTERN: Prototype Pattern - Retrieve and clone prototype template to prepopulate defaults
        String resolvedKey = (templateKey != null && !templateKey.trim().isEmpty()) ? templateKey : "BASIC";
        MembershipPrototype prototypeClone;
        try {
            prototypeClone = prototypeRegistry.getClonedPrototype(resolvedKey);
        } catch (Exception e) {
            prototypeClone = prototypeRegistry.getClonedPrototype("BASIC");
        }
        prototypeClone.applyToForm(form);

        model.addAttribute("enrollmentForm", form);
        model.addAttribute("selectedPrototype", prototypeClone);
        model.addAttribute("prototypes", prototypeRegistry.getAllPrototypes());
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
