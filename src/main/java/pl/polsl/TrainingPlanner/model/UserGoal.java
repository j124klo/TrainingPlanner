package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_goals")
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String description; // Np. "Chcę wycisnąć 100kg na klatę" lub "Zejść do 80kg"

    private LocalDate deadline; // Do kiedy cel ma być zrealizowany

    private boolean achieved = false; // Czy cel został osiągnięty

    public UserGoal() {}

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public boolean isAchieved() { return achieved; }
    public void setAchieved(boolean achieved) { this.achieved = achieved; }
}