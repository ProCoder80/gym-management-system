package com.gym.model.entity;

import java.time.LocalDateTime;

public class ClassBooking {
    private Long id;
    private Long classId;
    private Long memberId;
    private String memberName; // Display helper
    private String className;  // Display helper
    private LocalDateTime bookingDate;
    private String status;     // CONFIRMED, CANCELLED

    public ClassBooking() {
    }

    public ClassBooking(Long id, Long classId, Long memberId, LocalDateTime bookingDate, String status) {
        this.id = id;
        this.classId = classId;
        this.memberId = memberId;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
