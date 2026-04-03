package pl.polsl.TrainingPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.PlanDay;

public interface PlanDayRepository extends JpaRepository<PlanDay, Long> {
}