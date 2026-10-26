package com.gym.model.entity;

public class Trainer {
    private Long id;
    private String name;
    private String specialization;
    private boolean available; // true = Available, false = Off-duty/Unavailable
    private String contact;

    public Trainer() {
    }

    public Trainer(Long id, String name, String specialization, boolean available, String contact) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.available = available;
        this.contact = contact;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", specialization='" + specialization + '\'' +
                ", available=" + available +
                ", contact='" + contact + '\'' +
                '}';
    }
}
