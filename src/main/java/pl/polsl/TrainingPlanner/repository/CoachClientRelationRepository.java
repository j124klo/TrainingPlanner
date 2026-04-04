package pl.polsl.TrainingPlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.CoachClientRelation;
import java.util.List;

public interface CoachClientRelationRepository extends JpaRepository<CoachClientRelation, Long> {
    List<CoachClientRelation> findByCoachId(Long coachId);
    List<CoachClientRelation> findByClientId(Long clientId);
}