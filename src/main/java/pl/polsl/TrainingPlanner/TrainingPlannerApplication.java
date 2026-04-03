package pl.polsl.TrainingPlanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.Visibility;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;

@SpringBootApplication
public class TrainingPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingPlannerApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(ExerciseRepository exerciseRepository) {
		return args -> {
			// Jeśli tabela jest pusta, dodaj testowe dane
			if (exerciseRepository.count() == 0) {

				Exercise ex1 = new Exercise("Martwy Ciąg", "Podnoszenie sztangi z ziemi", "Siłowe");
				ex1.setVisibility(Visibility.PUBLIC); // Ustawiamy jako publiczne, żeby każdy je widział!

				Exercise ex2 = new Exercise("Wyciskanie na ławce", "Wyciskanie sztangi leżąc", "Siłowe");
				ex2.setVisibility(Visibility.PUBLIC);

				Exercise ex3 = new Exercise("Bieganie na bieżni", "Lekki trucht przez 30 minut", "Kardio");
				ex3.setVisibility(Visibility.PUBLIC);

				exerciseRepository.save(ex1);
				exerciseRepository.save(ex2);
				exerciseRepository.save(ex3);

				System.out.println("✅ Dodano testowe ćwiczenia (Siłowe i Kardio) do bazy!");
			}
		};
	}
}