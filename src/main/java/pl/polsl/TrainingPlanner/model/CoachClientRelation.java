package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coach_client_relations")
public class CoachClientRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // Status: "PENDING" (oczekujące na akceptacje klienta) lub "ACCEPTED"
    @Column(nullable = false)
    private String status = "PENDING";

    public CoachClientRelation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCoach() { return coach; }
    public void setCoach(User coach) { this.coach = coach; }
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}