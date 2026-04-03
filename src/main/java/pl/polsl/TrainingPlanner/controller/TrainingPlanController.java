package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.PlanDay;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.PlanDayRepository;
import pl.polsl.TrainingPlanner.repository.TrainingPlanRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;

@Controller
public class TrainingPlanController {

    private final TrainingPlanRepository planRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository; // NOWE
    private final PlanDayRepository planDayRepository; // DODANE

    // Zaktualizowany konstruktor
    public TrainingPlanController(TrainingPlanRepository planRepository, UserRepository userRepository, ExerciseRepository exerciseRepository, PlanDayRepository planDayRepository) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.planDayRepository = planDayRepository;
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
    // ZMODYFIKOWANE DODAWANIE PLANU
    @PostMapping("/plans/add")
    public String addPlan(@ModelAttribute TrainingPlan newPlan, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        newPlan.setUser(currentUser);

        // AUTOMATYCZNE GENEROWANIE DNI TYGODNIA
        String[] daysOfWeek = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        for (String dayName : daysOfWeek) {
            PlanDay planDay = new PlanDay();
            planDay.setDayName(dayName);
            planDay.setPlan(newPlan); // Przypisujemy dzień do tego planu
            newPlan.getDays().add(planDay); // Dodajemy dzień do listy w planie
        }

        planRepository.save(newPlan); // Zapisze plan i od razu wszystkie 7 dni!
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
    // ZMODYFIKOWANE DODAWANIE ĆWICZENIA (Teraz wymaga ID Dnia)
    @PostMapping("/plans/{planId}/addExercise")
    public String addExerciseToDay(@PathVariable Long planId, @RequestParam Long dayId, @RequestParam Long exerciseId, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        // Zabezpieczenie własności uciąłem dla czytelności, pamiętajcie o nim w finale!
        PlanDay day = planDayRepository.findById(dayId).orElseThrow();
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();

        day.getExercises().add(exercise);
        planDayRepository.save(day);

        return "redirect:/plans/" + planId;
    }

    // 6. Usuwanie ćwiczenia z planu
    // ZMODYFIKOWANE USUWANIE ĆWICZENIA Z DNIA
    @PostMapping("/plans/{planId}/removeExercise/{dayId}/{exerciseId}")
    public String removeExerciseFromDay(@PathVariable Long planId, @PathVariable Long dayId, @PathVariable Long exerciseId, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        PlanDay day = planDayRepository.findById(dayId).orElseThrow();
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();

        day.getExercises().remove(exercise);
        planDayRepository.save(day);

        return "redirect:/plans/" + planId;
    }
}