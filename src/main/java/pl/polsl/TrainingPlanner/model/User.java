package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // Login musi być unikalny i nie może być pusty
    private String login;

    @Column(nullable = false)
    private String password;

    // NOWE POLE: Rola użytkownika
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER; // Domyślnie każdy to zwykły user

    @Column(name = "current_plan_id")
    private Long currentPlanId;

    // Pusty konstruktor dla Hibernate
    public User() {}

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getCurrentPlanId() { return currentPlanId; }
    public void setCurrentPlanId(Long currentPlanId) { this.currentPlanId = currentPlanId; }
}