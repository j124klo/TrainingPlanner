package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.Visibility;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.service.AccessControlService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessService;

    public ExerciseController(ExerciseRepository exerciseRepository, UserRepository userRepository, AccessControlService accessService) {
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
    }

    // --- ZMODYFIKOWANA METODA showExercises ---
    @GetMapping("/exercises")
    public String showExercises(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        List<Exercise> allExercises = exerciseRepository.findAll();

        List<Exercise> visibleExercises = allExercises.stream()
                .filter(ex -> accessService.canViewExercise(ex, currentUser))
                .collect(Collectors.toList());

        List<Long> editableIds = visibleExercises.stream()
                .filter(ex -> accessService.canEditExercise(ex, currentUser))
                .map(Exercise::getId)
                .collect(Collectors.toList());

        model.addAttribute("exercisesList", visibleExercises);
        model.addAttribute("editableIds", editableIds);
        model.addAttribute("newExercise", new Exercise());

        // NOWE: Przekazujemy flagę, czy użytkownik to Trener lub Admin
        boolean isCoachOrAdmin = currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.COACH || currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.ADMIN;
        model.addAttribute("isCoachOrAdmin", isCoachOrAdmin);

        return "exercises-list";
    }

    // ... (tutaj zostają metody addExercise, deleteExercise, cloneExercise) ...

    // --- NOWE: EKRAN UDOSTĘPNIANIA ĆWICZENIA ---
    @GetMapping("/exercises/{id}/share")
    public String showShareForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        // Tylko trener i admin mają dostęp do tej funkcji
        if (currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.USER) return "redirect:/exercises";

        Exercise exercise = exerciseRepository.findById(id).orElseThrow();
        if (!accessService.canEditExercise(exercise, currentUser)) return "redirect:/exercises";

        model.addAttribute("exercise", exercise);

        String sharedLogins = exercise.getSharedWith().stream()
                .map(User::getLogin)
                .collect(Collectors.joining(", "));
        model.addAttribute("sharedLogins", sharedLogins);

        return "share-exercise";
    }

    // --- NOWE: ZAPISYWANIE UDOSTĘPNIANIA ĆWICZENIA ---
    @PostMapping("/exercises/{id}/share")
    public String shareExercise(@PathVariable Long id,
                                @RequestParam Visibility visibility,
                                @RequestParam(required = false) String logins,
                                HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        if (currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.USER) return "redirect:/exercises";

        Exercise exercise = exerciseRepository.findById(id).orElseThrow();
        if (!accessService.canEditExercise(exercise, currentUser)) return "redirect:/exercises";

        // Aktualizujemy status i czyścimy starą listę
        exercise.setVisibility(visibility);
        exercise.getSharedWith().clear();

        // Jeśli wybrano SHARED, dodajemy użytkowników po loginach
        if (visibility == Visibility.SHARED && logins != null && !logins.isEmpty()) {
            String[] loginArray = logins.split(",");
            for (String login : loginArray) {
                userRepository.findByLogin(login.trim()).ifPresent(exercise.getSharedWith()::add);
            }
        }

        exerciseRepository.save(exercise);
        return "redirect:/exercises";
    }

    @PostMapping("/exercises/add")
    public String addExercise(@ModelAttribute Exercise newExercise, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        newExercise.setOwner(currentUser); // Ustawiamy właściciela!
        newExercise.setVisibility(Visibility.PRIVATE); // Domyślnie prywatne

        exerciseRepository.save(newExercise);
        return "redirect:/exercises";
    }

    @PostMapping("/exercises/delete/{id}")
    public String deleteExercise(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        Exercise exercise = exerciseRepository.findById(id).orElseThrow();

        // Zabezpieczenie backendowe
        if (accessService.canEditExercise(exercise, currentUser)) {
            exerciseRepository.deleteById(id);
        }
        return "redirect:/exercises";
    }

    // --- NOWOŚĆ: KLONOWANIE ĆWICZENIA ---
    @PostMapping("/exercises/clone/{id}")
    public String cloneExercise(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        Exercise original = exerciseRepository.findById(id).orElseThrow();

        // Można sklonować tylko to, co się widzi
        if (accessService.canViewExercise(original, currentUser)) {
            Exercise clone = new Exercise();
            clone.setName(original.getName() + " (Kopia)");
            clone.setDescription(original.getDescription());
            clone.setValueTypes(original.getValueTypes());
            clone.setOwner(currentUser); // Ty stajesz się właścicielem kopii!
            clone.setVisibility(Visibility.PRIVATE);
            exerciseRepository.save(clone);
        }

        return "redirect:/exercises";
    }
}