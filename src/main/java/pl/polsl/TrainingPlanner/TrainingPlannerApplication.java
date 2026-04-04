package pl.polsl.TrainingPlanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.WorkoutLog;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.repository.WorkoutLogRepository;

import java.time.LocalDate;

@SpringBootApplication
public class TrainingPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingPlannerApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(ExerciseRepository exerciseRepository, UserRepository userRepository, WorkoutLogRepository workoutLogRepository) {
		return args -> {
			if (exerciseRepository.count() == 0) {
				// ZMIENIONE TYPY NA NOWE!
				Exercise ex1 = new Exercise("Martwy Ciąg", "Sztanga", "Ciężar + Powtórzenia");
				ex1.setPublic(true);
				Exercise ex2 = new Exercise("Pompki", "Bodyweight", "Tylko Powtórzenia");
				ex2.setPublic(true);
				Exercise ex3 = new Exercise("Bieganie", "Bieżnia", "Dystans + Czas");
				ex3.setPublic(true);
				Exercise ex4 = new Exercise("Deska (Plank)", "Izometria", "Tylko Czas");
				ex4.setPublic(true);

				exerciseRepository.save(ex1); exerciseRepository.save(ex2);
				exerciseRepository.save(ex3); exerciseRepository.save(ex4);

				if (userRepository.count() == 0) {
					User testUser = new User();
					testUser.setLogin("test"); testUser.setPassword("test");
					testUser.setRole(pl.polsl.TrainingPlanner.model.Role.USER);
					userRepository.save(testUser);

					// Przykładowe logi
					WorkoutLog log1 = new WorkoutLog();
					log1.setUser(testUser); log1.setExercise(ex1); log1.setDate(LocalDate.now());
					log1.setSetNumber(1); log1.setWeight(120.0f); log1.setReps(1);
					workoutLogRepository.save(log1);

					WorkoutLog log2 = new WorkoutLog();
					log2.setUser(testUser); log2.setExercise(ex2); log2.setDate(LocalDate.now());
					log2.setSetNumber(1); log2.setReps(25); // Max Endurance
					workoutLogRepository.save(log2);

					WorkoutLog log3 = new WorkoutLog();
					log3.setUser(testUser); log3.setExercise(ex3); log3.setDate(LocalDate.now());
					log3.setSetNumber(1); log3.setDistance(10.5f); log3.setTimeMinutes(50); // Max Distance & Time
					workoutLogRepository.save(log3);
				}
			}
		};
	}
}