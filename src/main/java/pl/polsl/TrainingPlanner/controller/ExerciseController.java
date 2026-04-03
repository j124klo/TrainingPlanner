package pl.polsl.TrainingPlanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;

@Controller
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;

    public ExerciseController(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    // 1. Wyświetlanie strony
    @GetMapping("/exercises")
    public String showExercises(Model model) {
        model.addAttribute("exercisesList", exerciseRepository.findAll());

        // Wysyłamy pusty obiekt Exercise do formularza
        model.addAttribute("newExercise", new Exercise());

        return "exercises-list";
    }

    // 2. Odbieranie danych z formularza
    @PostMapping("/exercises/add")
    public String addExercise(@ModelAttribute Exercise newExercise) {
        // Zapisujemy nowe ćwiczenie prosto do bazy PostgreSQL!
        exerciseRepository.save(newExercise);

        // Zamiast zwracać plik HTML, robimy "redirect", czyli każemy przeglądarce
        // odświeżyć stronę pod adresem /exercises, żeby zobaczyć nową listę.
        return "redirect:/exercises";
    }

    // 3. Usuwanie ćwiczenia z bazy
    @PostMapping("/exercises/delete/{id}")
    public String deleteExercise(@org.springframework.web.bind.annotation.PathVariable Long id) {
        exerciseRepository.deleteById(id);
        return "redirect:/exercises"; // Po usunięciu odśwież stronę
    }
}