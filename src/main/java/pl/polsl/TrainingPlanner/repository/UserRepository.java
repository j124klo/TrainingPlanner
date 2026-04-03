package pl.polsl.TrainingPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.User;

// Podajemy klasę (User) i typ jej klucza głównego (Long)
public interface UserRepository extends JpaRepository<User, Long> {
    // Macie tu już gotowe metody np. findAll(), save(), deleteById()
}