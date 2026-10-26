package com.gym.controller;

import com.gym.model.dto.PlanChangeForm;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.model.entity.PlanHistoryEntry;
import com.gym.service.MemberService;
import com.gym.service.PlanManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller strictly handling HTTP requests for Member Plan management
 * via the Memento Pattern (upgrade, downgrade, undo, and history audit trail).
 */
@Controller
@RequestMapping("/plans")
public class PlanController {

    private final PlanManagementService planManagementService;
    private final MemberService memberService;

    public PlanController(PlanManagementService planManagementService, MemberService memberService) {
        this.planManagementService = planManagementService;
        this.memberService = memberService;
    }

    @GetMapping("/{memberId}")
    public String viewMemberPlan(@PathVariable Long memberId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Member> memberOpt = memberService.getMemberById(memberId);
        if (memberOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Member with ID " + memberId + " not found.");
            return "redirect:/members";
        }

        Member member = memberOpt.get();
        Optional<Membership> activeMembership = planManagementService.getActiveMembership(memberId);
        List<PlanHistoryEntry> historyStack = planManagementService.getPlanHistory(memberId);

        model.addAttribute("member", member);
        model.addAttribute("activeMembership", activeMembership.orElse(null));
        model.addAttribute("historyStack", historyStack);
        model.addAttribute("hasHistoryToUndo", historyStack.size() > 1);

        PlanChangeForm form = new PlanChangeForm();
        form.setMemberId(memberId);
        if (activeMembership.isPresent()) {
            form.setNewPlanType(activeMembership.get().getPlanType());
            form.setLocker(activeMembership.get().isLocker());
            form.setDietPlan(activeMembership.get().isDietPlan());
            form.setPtSessions(activeMembership.get().getPtSessions());
        }
        model.addAttribute("planChangeForm", form);

        return "plans";
    }

    @PostMapping("/{memberId}/upgrade")
    public String upgradePlan(@PathVariable Long memberId,
                              @ModelAttribute("planChangeForm") PlanChangeForm form,
                              RedirectAttributes redirectAttributes) {
        try {
            boolean success = planManagementService.upgradePlan(
                    memberId,
                    form.getNewPlanType(),
                    form.isLocker(),
                    form.isDietPlan(),
                    form.getPtSessions()
            );
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "Plan successfully UPGRADED to " + form.getNewPlanType() + "! State snapshot saved in Memento history.");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Plan upgrade could not be completed.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Upgrade failed: " + e.getMessage());
        }
        return "redirect:/plans/" + memberId;
    }

    @PostMapping("/{memberId}/downgrade")
    public String downgradePlan(@PathVariable Long memberId,
                                @ModelAttribute("planChangeForm") PlanChangeForm form,
                                RedirectAttributes redirectAttributes) {
        try {
            boolean success = planManagementService.downgradePlan(
                    memberId,
                    form.getNewPlanType(),
                    form.isLocker(),
                    form.isDietPlan(),
                    form.getPtSessions()
            );
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "Plan successfully DOWNGRADED to " + form.getNewPlanType() + "! State snapshot saved in Memento history.");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Plan downgrade could not be completed.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Downgrade failed: " + e.getMessage());
        }
        return "redirect:/plans/" + memberId;
    }

    @PostMapping("/{memberId}/undo")
    public String undoPlanChange(@PathVariable Long memberId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // PATTERN: Restores previous state from Memento stack in SQLite
            boolean success = planManagementService.undoPlanChange(memberId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "↺ Successfully performed UNDO! Plan state restored to previous Memento snapshot.");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Undo could not be executed.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Undo Failed: " + e.getMessage());
        }
        return "redirect:/plans/" + memberId;
    }
}
