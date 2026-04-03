package pl.polsl.TrainingPlanner;// Zmień import na nasze nowe repozytorium i model
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.polsl.TrainingPlanner.model.Exercise;
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
				exerciseRepository.save(new Exercise("Martwy Ciąg", "Podnoszenie sztangi z ziemi", "Plecy/Nogi"));
				exerciseRepository.save(new Exercise("Wyciskanie na ławce", "Wyciskanie sztangi leżąc", "Klatka piersiowa"));
				exerciseRepository.save(new Exercise("Podciąganie", "Podciąganie na drążku nachwytem", "Plecy"));
				System.out.println("✅ Dodano testowe ćwiczenia do bazy!");
			}
		};
	}
}