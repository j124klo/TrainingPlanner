package pl.polsl.TrainingPlanner.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.TrainingPlanner.model.BodyMeasurement;
public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {}