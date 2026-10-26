package com.gym.service;

import com.gym.dao.MemberDAO;
import com.gym.model.entity.Member;
import com.gym.pattern.mediator.GymMediator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MemberService Colleague in Mediator Pattern.
 * STRICT RULE: Does NOT reference TrainerService or ClassScheduleService directly.
 * All cross-system notifications and coordinations occur via GymMediator.
 */
@Service
public class MemberService {

    private final MemberDAO memberDAO;
    private final GymMediator gymMediator;

    public MemberService(MemberDAO memberDAO, GymMediator gymMediator) {
        this.memberDAO = memberDAO;
        this.gymMediator = gymMediator;
    }

    public List<Member> getAllMembers() {
        return memberDAO.findAll();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberDAO.findById(id);
    }

    public Optional<Member> getMemberByEmail(String email) {
        return memberDAO.findByEmail(email);
    }

    public Long createMember(Member member) {
        if (member.getJoinDate() == null) {
            member.setJoinDate(LocalDate.now());
        }
        if (member.getStatus() == null || member.getStatus().isEmpty()) {
            member.setStatus("ACTIVE");
        }
        Long id = memberDAO.create(member);
        gymMediator.notify(this, "MEMBER_CREATED", member);
        return id;
    }

    public boolean updateMember(Member member) {
        boolean updated = memberDAO.update(member);
        if (updated) {
            gymMediator.notify(this, "MEMBER_UPDATED", member);
        }
        return updated;
    }

    public boolean deleteMember(Long id) {
        boolean deleted = memberDAO.delete(id);
        if (deleted) {
            gymMediator.notify(this, "MEMBER_DELETED", id);
        }
        return deleted;
    }

    public int countMembers() {
        return memberDAO.count();
    }
}
