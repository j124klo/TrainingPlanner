package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exercises") // Nazwa tabeli w PostgreSQL
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String targetMuscle; // np. Klatka, Plecy, Nogi

    // 1. Pusty konstruktor (wymagany przez Springa/Hibernate)
    public Exercise() {
    }

    // 2. Konstruktor ułatwiający dodawanie z kodu
    public Exercise(String name, String description, String targetMuscle) {
        this.name = name;
        this.description = description;
        this.targetMuscle = targetMuscle;
    }

    // 3. Gettery i Settery (Konieczne dla Thymeleaf i bazy danych!)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTargetMuscle() { return targetMuscle; }
    public void setTargetMuscle(String targetMuscle) { this.targetMuscle = targetMuscle; }
}