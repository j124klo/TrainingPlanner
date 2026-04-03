package pl.polsl.TrainingPlanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class TrainingPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingPlannerApplication.class, args);
	}

	// Ten kawałek kodu Spring Boot uruchomi automatycznie zaraz po udanym starcie serwera
	@Bean
	public CommandLineRunner testDatabaseConnection(JdbcTemplate jdbcTemplate) {
		return args -> {
			System.out.println("\n=======================================================");
			System.out.println("🏋️ TEST POŁĄCZENIA Z BAZĄ DANYCH APLIKACJI TRENINGOWEJ 🏋️");

			try {
				// Wykonujemy proste zapytanie SQL, żeby pobrać użytkowników, których wcześniej dodaliśmy
				String sql = "SELECT id, login, role FROM users";
				List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);

				if (users.isEmpty()) {
					System.out.println("Baza odpowiada, ale tabela 'users' jest pusta!");
				} else {
					System.out.println("Sukces! Znalezieni użytkownicy w bazie:");
					for (Map<String, Object> user : users) {
						System.out.println(" -> ID: " + user.get("id") + " | Login: " + user.get("login") + " | Rola: " + user.get("role"));
					}
				}
			} catch (Exception e) {
				System.out.println("❌ Wystąpił błąd podczas pobierania danych z tabeli 'users'!");
				System.out.println("Szczegóły błędu: " + e.getMessage());
			}

			System.out.println("=======================================================\n");
		};
	}
}