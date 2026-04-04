package pl.polsl.TrainingPlanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.polsl.TrainingPlanner.model.*;
import pl.polsl.TrainingPlanner.repository.*;

import java.time.LocalDate;

@SpringBootApplication
public class TrainingPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingPlannerApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(ExerciseRepository exRepo, UserRepository userRepo, WorkoutLogRepository logRepo, TrainingPlanRepository planRepo, CoachClientRelationRepository relRepo) {
		return args -> {
			if (exRepo.count() == 0) {
				Exercise ex1 = new Exercise("Martwy Ciąg", "Sztanga", "Ciężar + Powtórzenia"); ex1.setPublic(true);
				Exercise ex2 = new Exercise("Wyciskanie", "Ławka", "Ciężar + Powtórzenia"); ex2.setPublic(true);
				Exercise ex3 = new Exercise("Bieganie", "Bieżnia", "Dystans + Czas"); ex3.setPublic(true);
				exRepo.save(ex1); exRepo.save(ex2); exRepo.save(ex3);

				if (userRepo.count() == 0) {
					// 1. Użytkownik (Klient)
					User testUser = new User();
					testUser.setLogin("test"); testUser.setPassword("test");
					testUser.setRole(Role.USER);
					userRepo.save(testUser);

					// 2. Trener
					User coachUser = new User();
					coachUser.setLogin("trener"); coachUser.setPassword("trener");
					coachUser.setRole(Role.COACH);
					userRepo.save(coachUser);

					// 3. Relacja Trener-Klient
					CoachClientRelation rel = new CoachClientRelation();
					rel.setCoach(coachUser); rel.setClient(testUser); rel.setStatus("ACCEPTED");
					relRepo.save(rel);

					// 4. Plan dla trenera (żeby mógł go przypisać klientowi)
					TrainingPlan coachPlan = new TrainingPlan();
					coachPlan.setName("Plan Siłowy od Trenera");
					coachPlan.setDescription("Zbuduj siłę w 4 tygodnie");
					coachPlan.setUser(coachUser);
					coachPlan.setPublic(false);
					planRepo.save(coachPlan);

					// 5. Generowanie 15 dni treningowych (co 2 dni wstecz)
					float startingWeight = 100.0f;
					for (int i = 30; i >= 0; i -= 2) {
						LocalDate workoutDate = LocalDate.now().minusDays(i);

						// Ćwiczenie 1: Martwy Ciąg (3 serie)
						for(int set = 1; set <= 3; set++) {
							WorkoutLog log1 = new WorkoutLog();
							log1.setUser(testUser); log1.setExercise(ex1); log1.setDate(workoutDate);
							log1.setSetNumber(set); log1.setReps(5); log1.setWeight(startingWeight + (30-i)); // Ciężar rośnie!
							logRepo.save(log1);
						}
						// Ćwiczenie 2: Bieganie (1 seria)
						WorkoutLog log2 = new WorkoutLog();
						log2.setUser(testUser); log2.setExercise(ex3); log2.setDate(workoutDate);
						log2.setSetNumber(1); log2.setDistance(5.0f); log2.setTimeMinutes(30);
						logRepo.save(log2);
					}
					System.out.println("✅ Wygenerowano bazę danych, konta 'test' i 'trener' oraz historię z 30 dni!");
				}
			}
		};
	}
}