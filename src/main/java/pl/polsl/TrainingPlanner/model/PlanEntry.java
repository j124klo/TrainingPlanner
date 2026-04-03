package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_entries")
public class PlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private TrainingPlan plan;

    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1 = Poniedziałek, 7 = Niedziela

    public PlanEntry() {}

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TrainingPlan getPlan() { return plan; }
    public void setPlan(TrainingPlan plan) { this.plan = plan; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
}