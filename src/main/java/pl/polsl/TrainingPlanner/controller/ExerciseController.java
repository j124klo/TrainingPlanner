package pl.polsl.TrainingPlanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;

@Controller
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;

    // Wstrzykujemy repozytorium przez konstruktor
    public ExerciseController(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    // Gdy użytkownik wejdzie na adres: localhost:8080/exercises
    @GetMapping("/exercises")
    public String showExercises(Model model) {
        // Pobierz wszystko z bazy i prześlij do widoku pod nazwą "exercisesList"
        model.addAttribute("exercisesList", exerciseRepository.findAll());

        // Zwróć nazwę pliku HTML (bez końcówki .html)
        return "exercises-list";
    }
}