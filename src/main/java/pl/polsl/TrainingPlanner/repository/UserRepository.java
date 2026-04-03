package pl.polsl.TrainingPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Zwraca Optional (czyli może znaleźć użytkownika, ale może też zwrócić "pusto", jeśli taki login nie istnieje)
    Optional<User> findByLogin(String login);
}