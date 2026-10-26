package com.gym.model.entity;

public class GymClass {
    private Long id;
    private String name;
    private String description;
    private Long trainerId;
    private String trainerName; // Display helper
    private boolean trainerAvailable; // Trainer availability helper
    private String scheduleTime;
    private int capacity;
    private int currentEnrolled;

    public GymClass() {
    }

    public GymClass(Long id, String name, String description, Long trainerId, String scheduleTime, int capacity, int currentEnrolled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.trainerId = trainerId;
        this.scheduleTime = scheduleTime;
        this.capacity = capacity;
        this.currentEnrolled = currentEnrolled;
    }

    public boolean isFull() {
        return currentEnrolled >= capacity;
    }

    public int getAvailableSpots() {
        return Math.max(0, capacity - currentEnrolled);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public boolean isTrainerAvailable() {
        return trainerAvailable;
    }

    public void setTrainerAvailable(boolean trainerAvailable) {
        this.trainerAvailable = trainerAvailable;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentEnrolled() {
        return currentEnrolled;
    }

    public void setCurrentEnrolled(int currentEnrolled) {
        this.currentEnrolled = currentEnrolled;
    }

    @Override
    public String toString() {
        return "GymClass{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", trainerId=" + trainerId +
                ", scheduleTime='" + scheduleTime + '\'' +
                ", capacity=" + capacity +
                ", currentEnrolled=" + currentEnrolled +
                '}';
    }
}
