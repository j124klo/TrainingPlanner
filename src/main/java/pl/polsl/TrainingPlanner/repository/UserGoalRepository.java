package pl.polsl.TrainingPlanner.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.UserGoal;
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {}