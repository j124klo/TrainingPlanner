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

    @Column(name = "value_types")
    private String valueTypes;

    // 1. Kto stworzył to ćwiczenie
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // 3. Lista osób, którym udostępniono to ćwiczenie (jeśli status to SHARED)
    @Column(name = "is_public")
    private boolean isPublic = false; // Domyślnie prywatne

    // 1. Pusty konstruktor (wymagany przez Springa/Hibernate)
    public Exercise() {
    }

    // 2. Konstruktor ułatwiający dodawanie z kodu
    public Exercise(String name, String description, String valueTypes) {
        this.name = name;
        this.description = description;
        this.valueTypes = valueTypes;
    }

    // 3. Gettery i Settery (Konieczne dla Thymeleaf i bazy danych!)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getValueTypes() { return valueTypes; }
    public void setValueTypes(String valueTypes) { this.valueTypes = valueTypes; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }


}