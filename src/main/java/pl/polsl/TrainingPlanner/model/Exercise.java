package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercises") // Nazwa tabeli w PostgreSQL
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String targetMuscle; // np. Klatka, Plecy, Nogi

    // 1. Kto stworzył to ćwiczenie
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // 2. Status widoczności (Domyślnie prywatne)
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE;

    // 3. Lista osób, którym udostępniono to ćwiczenie (jeśli status to SHARED)
    @ManyToMany
    @JoinTable(
            name = "exercise_shared",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> sharedWith = new ArrayList<>();

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

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public List<User> getSharedWith() { return sharedWith; }
    public void setSharedWith(List<User> sharedWith) { this.sharedWith = sharedWith; }
}