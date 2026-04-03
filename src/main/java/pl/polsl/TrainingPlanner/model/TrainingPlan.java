package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "training_plans")
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    // Relacja: Wiele planów należy do jednego użytkownika
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ... poprzednie zmienne (id, name, description, user) ...

    // Relacja Wiele-do-Wielu: Plan <-> Ćwiczenia
    @ManyToMany
    @JoinTable(
            name = "plan_exercises", // Spring utworzy dodatkową tabelkę łączącą
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id")
    )
    private List<Exercise> exercises = new ArrayList<>();

    // --- PAMIĘTAJ O DODANIU GETTERA I SETTERA NA DOLE KLASY ---
    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }

    public TrainingPlan() {}

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}