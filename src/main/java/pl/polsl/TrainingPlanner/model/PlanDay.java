package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_days")
public class PlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dayName; // Np. "Poniedziałek"

    // Ten dzień należy do konkretnego Planu
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private TrainingPlan plan;

    // Relacja Wiele-do-Wielu: Dzień <-> Ćwiczenia (zamiast Plan <-> Ćwiczenia)
    @ManyToMany
    @JoinTable(
            name = "day_exercises",
            joinColumns = @JoinColumn(name = "day_id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id")
    )
    private List<Exercise> exercises = new ArrayList<>();

    public PlanDay() {}

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }

    public TrainingPlan getPlan() { return plan; }
    public void setPlan(TrainingPlan plan) { this.plan = plan; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }
}