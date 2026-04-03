package pl.polsl.TrainingPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.PlanEntry;

public interface PlanEntryRepository extends JpaRepository<PlanEntry, Long> {
}