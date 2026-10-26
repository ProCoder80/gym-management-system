package com.gym.controller;

import com.gym.model.entity.Member;
import com.gym.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller strictly handling HTTP requests for Member management (CRUD).
 */
@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        model.addAttribute("newMember", new Member());
        return "members";
    }

    @PostMapping("/add")
    public String addMember(@ModelAttribute("newMember") Member member,
                            RedirectAttributes redirectAttributes) {
        try {
            if (member.getName() == null || member.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "Member name cannot be blank.");
                return "redirect:/members";
            }
            if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "Member email is required.");
                return "redirect:/members";
            }
            if (memberService.getMemberByEmail(member.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMsg", "A member with email '" + member.getEmail() + "' already exists.");
                return "redirect:/members";
            }

            memberService.createMember(member);
            redirectAttributes.addFlashAttribute("successMsg", "Member '" + member.getName() + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to create member: " + e.getMessage());
        }
        return "redirect:/members";
    }

    @PostMapping("/edit")
    public String editMember(@ModelAttribute Member member,
                             RedirectAttributes redirectAttributes) {
        try {
            if (member.getId() == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "Member ID missing.");
                return "redirect:/members";
            }

            boolean updated = memberService.updateMember(member);
            if (updated) {
                redirectAttributes.addFlashAttribute("successMsg", "Member details updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Failed to update member. Record not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error updating member: " + e.getMessage());
        }
        return "redirect:/members";
    }

    @PostMapping("/delete/{id}")
    public String deleteMember(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Member> member = memberService.getMemberById(id);
            if (member.isPresent()) {
                memberService.deleteMember(id);
                redirectAttributes.addFlashAttribute("successMsg", "Member '" + member.get().getName() + "' removed successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Member not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Cannot delete member: " + e.getMessage());
        }
        return "redirect:/members";
    }
}
