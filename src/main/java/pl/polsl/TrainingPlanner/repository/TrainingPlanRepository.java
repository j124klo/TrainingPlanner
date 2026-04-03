package pl.polsl.TrainingPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;

import java.util.List;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    // Spring sam wygeneruje SQL: SELECT * FROM training_plans WHERE user_id = ?
    List<TrainingPlan> findByUser(User user);
}