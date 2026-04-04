package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "body_measurements")
public class BodyMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    // Waga w kg
    private Float weight;

    // Poziom tkanki tłuszczowej w %
    @Column(name = "body_fat")
    private Float bodyFat;

    public BodyMeasurement() {}

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Float getWeight() { return weight; }
    public void setWeight(Float weight) { this.weight = weight; }
    public Float getBodyFat() { return bodyFat; }
    public void setBodyFat(Float bodyFat) { this.bodyFat = bodyFat; }
}