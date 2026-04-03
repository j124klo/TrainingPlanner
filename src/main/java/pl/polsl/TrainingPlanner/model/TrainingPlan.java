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

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE;

    @ManyToMany
    @JoinTable(
            name = "plan_shared",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> sharedWith = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanEntry> planEntries = new ArrayList<>();

    public List<PlanEntry> getPlanEntries() { return planEntries; }
    public void setPlanEntries(List<PlanEntry> planEntries) { this.planEntries = planEntries; }

    public TrainingPlan() {}

    // Gettery i Settery
    // ---user---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // ---misc---
    public Visibility getVisibility() { return visibility; }
    public void setSharedWith(List<User> sharedWith) { this.sharedWith = sharedWith; }

    public List<User> getSharedWith() { return sharedWith; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    }