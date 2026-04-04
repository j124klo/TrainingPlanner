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
			// Inicjalizacja ćwiczeń
			if (exerciseRepository.count() == 0) {
				Exercise ex1 = new Exercise("Martwy Ciąg", "Podnoszenie sztangi z ziemi", "Siłowe");
				ex1.setPublic(true);
				Exercise ex2 = new Exercise("Wyciskanie na ławce", "Wyciskanie sztangi leżąc", "Siłowe");
				ex2.setPublic(true);
				Exercise ex3 = new Exercise("Podciąganie", "Z ciężarem własnego ciała", "Siłowe");
				ex3.setPublic(true);

				exerciseRepository.save(ex1);
				exerciseRepository.save(ex2);
				exerciseRepository.save(ex3);
				System.out.println("✅ Dodano testowe ćwiczenia do bazy!");

				// Jeśli nie ma użytkowników, stwórzmy konto testowe z logami
				if (userRepository.count() == 0) {
					User testUser = new User();
					testUser.setLogin("test");
					testUser.setPassword("test"); // Konto do testów!
					testUser.setRole(pl.polsl.TrainingPlanner.model.Role.USER);
					userRepository.save(testUser);

					// Generujemy logi dla usera "test"
					WorkoutLog log1 = new WorkoutLog();
					log1.setUser(testUser);
					log1.setExercise(ex1);
					log1.setDate(LocalDate.now()); // Dzisiaj
					log1.setSetNumber(1);
					log1.setWeight(180.0f);
					log1.setReps(1);
					workoutLogRepository.save(log1);

					WorkoutLog log2 = new WorkoutLog();
					log2.setUser(testUser);
					log2.setExercise(ex2);
					log2.setDate(LocalDate.now().minusDays(2)); // 2 dni temu
					log2.setSetNumber(3);
					log2.setWeight(100.0f);
					log2.setReps(5);
					workoutLogRepository.save(log2);

					WorkoutLog log3 = new WorkoutLog();
					log3.setUser(testUser);
					log3.setExercise(ex3);
					log3.setDate(LocalDate.now().minusDays(7)); // Tydzień temu
					log3.setSetNumber(2);
					log3.setWeight(20.0f); // Dodatkowy ciężar
					log3.setReps(8);
					workoutLogRepository.save(log3);

					System.out.println("✅ Dodano użytkownika 'test' (hasło: test) oraz jego historię logów!");
				}
			}
		};
	}
}