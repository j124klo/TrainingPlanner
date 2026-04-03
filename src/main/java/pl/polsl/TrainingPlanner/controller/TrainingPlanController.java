package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.TrainingPlanRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;

@Controller
public class TrainingPlanController {

    private final TrainingPlanRepository planRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository; // NOWE

    // Zaktualizowany konstruktor
    public TrainingPlanController(TrainingPlanRepository planRepository, UserRepository userRepository, ExerciseRepository exerciseRepository) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // 1. Wyświetlanie planów zalogowanego użytkownika
    @GetMapping("/plans")
    public String showPlans(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login"; // Blokada dla niezalogowanych

        // Szukamy użytkownika w bazie na podstawie ID z sesji
        User currentUser = userRepository.findById(userId).orElseThrow();

        // Przekazujemy do HTMLa tylko plany tego użytkownika
        model.addAttribute("plansList", planRepository.findByUser(currentUser));
        model.addAttribute("newPlan", new TrainingPlan());

        return "training-plans";
    }

    // 2. Dodawanie nowego planu
    @PostMapping("/plans/add")
    public String addPlan(@ModelAttribute TrainingPlan newPlan, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();

        // Przypisujemy plan do zalogowanego użytkownika przed zapisem do bazy!
        newPlan.setUser(currentUser);
        planRepository.save(newPlan);

        return "redirect:/plans";
    }

    // 3. Usuwanie planu
    @PostMapping("/plans/delete/{id}")
    public String deletePlan(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        planRepository.deleteById(id);
        return "redirect:/plans";
    }

    // 4. Ekran szczegółów planu (Lista ćwiczeń w planie)
    @GetMapping("/plans/{id}")
    public String showPlanDetails(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        TrainingPlan plan = planRepository.findById(id).orElseThrow();

        // Zabezpieczenie: sprawdzamy czy zalogowany użytkownik jest właścicielem tego planu!
        if (!plan.getUser().getId().equals(session.getAttribute("userId"))) {
            return "redirect:/plans"; // Ktoś próbuje wejść w nie swój plan
        }

        model.addAttribute("plan", plan);
        // Przesyłamy też wszystkie dostępne ćwiczenia, żeby móc je wybrać z rozwijanej listy
        model.addAttribute("allExercises", exerciseRepository.findAll());

        return "plan-details";
    }

    // 5. Dodawanie ćwiczenia do planu
    @PostMapping("/plans/{planId}/addExercise")
    public String addExerciseToPlan(@PathVariable Long planId, @RequestParam Long exerciseId, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        TrainingPlan plan = planRepository.findById(planId).orElseThrow();
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();

        // Dodajemy ćwiczenie do listy w planie i zapisujemy
        plan.getExercises().add(exercise);
        planRepository.save(plan);

        return "redirect:/plans/" + planId; // Wracamy na stronę szczegółów tego planu
    }
}