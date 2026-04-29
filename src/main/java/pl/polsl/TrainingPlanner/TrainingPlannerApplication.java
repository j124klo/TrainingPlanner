package pl.polsl.TrainingPlanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.polsl.TrainingPlanner.model.*;
import pl.polsl.TrainingPlanner.repository.*;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class TrainingPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingPlannerApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(ExerciseRepository exRepo, UserRepository userRepo, WorkoutLogRepository logRepo, TrainingPlanRepository planRepo, CoachClientRelationRepository relRepo) {
		return args -> {
			if (exRepo.count() == 0) {
				Exercise squat = new Exercise("Squat", "Barbell back squat", "Weight + Reps");
				Exercise bench = new Exercise("Bench Press", "Flat barbell bench press", "Weight + Reps");
				Exercise deadlift = new Exercise("Deadlift", "Conventional barbell deadlift", "Weight + Reps");
				Exercise ohp = new Exercise("Overhead Press", "Standing barbell press", "Weight + Reps");
				Exercise row = new Exercise("Barbell Row", "Bent over barbell row", "Weight + Reps");
				Exercise pullups = new Exercise("Pull-ups", "Wide grip bodyweight pull-ups", "Reps Only");
				Exercise pushups = new Exercise("Push-ups", "Standard floor push-ups", "Reps Only");
				Exercise run = new Exercise("Running", "Outdoor or treadmill running", "Distance + Time");
				Exercise plank = new Exercise("Plank", "Core stability hold", "Time Only");
				Exercise curls = new Exercise("Bicep Curls", "Dumbbell or barbell curls", "Weight + Reps");
				Exercise legPress = new Exercise("Leg Press", "Machine leg press", "Weight + Reps");
				Exercise lunges = new Exercise("Walking Lunges", "Dumbbell walking lunges", "Weight + Reps");
				Exercise dips = new Exercise("Tricep Dips", "Parallel bar bodyweight dips", "Reps Only");
				Exercise cycling = new Exercise("Cycling", "Stationary bike", "Distance + Time");
				Exercise jumpRope = new Exercise("Jump Rope", "Cardio skipping rope", "Time Only");

				squat.setPublic(true); bench.setPublic(true); deadlift.setPublic(true);
				ohp.setPublic(true); row.setPublic(true); pullups.setPublic(true);
				pushups.setPublic(true); run.setPublic(true); plank.setPublic(true); curls.setPublic(true);
				legPress.setPublic(true); lunges.setPublic(true); dips.setPublic(true); cycling.setPublic(true);
				jumpRope.setPublic(true);

				exRepo.saveAll(List.of(squat, bench, deadlift, ohp, row, pullups, pushups, run, plank, curls, legPress, lunges, dips, cycling, jumpRope));

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
					coachPlan.setName("Coach's Strength Plan");
					coachPlan.setDescription("Build strength in 4 weeks");
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
							log1.setUser(testUser); log1.setExercise(deadlift); log1.setDate(workoutDate);
							log1.setSetNumber(set); log1.setReps(5); log1.setWeight(startingWeight + (30-i)); // Ciężar rośnie!
							logRepo.save(log1);
						}
						// Ćwiczenie 2: Bieganie (1 seria)
						WorkoutLog log2 = new WorkoutLog();
						log2.setUser(testUser); log2.setExercise(run); log2.setDate(workoutDate);
						log2.setSetNumber(1); log2.setDistance(5.0f); log2.setTimeMinutes(30);
						logRepo.save(log2);
					}
					System.out.println("✅ Wygenerowano bazę danych, konta 'test' i 'trener' oraz historię z 30 dni!");
				}
			}
		};
	}
}