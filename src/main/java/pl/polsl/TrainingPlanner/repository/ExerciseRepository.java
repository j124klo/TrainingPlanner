package pl.polsl.TrainingPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.Exercise;

// JpaRepository<TypObiektu, TypKluczaGłównego>
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    // Nie musisz tu nic wpisywać!
    // Spring sam dostarczy metody takie jak findAll(), save(), deleteById()
}