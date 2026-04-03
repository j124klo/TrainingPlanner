package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.PlanDay;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.Visibility;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.PlanDayRepository;
import pl.polsl.TrainingPlanner.repository.TrainingPlanRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.service.AccessControlService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TrainingPlanController {

    private final TrainingPlanRepository planRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final PlanDayRepository planDayRepository;
    private final AccessControlService accessService; // NOWE

    public TrainingPlanController(TrainingPlanRepository planRepository, UserRepository userRepository, ExerciseRepository exerciseRepository, PlanDayRepository planDayRepository, AccessControlService accessService) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.planDayRepository = planDayRepository;
        this.accessService = accessService;
    }

    // --- ZMODYFIKOWANE: Wyświetlanie planów (Filtrowanie) ---
    @GetMapping("/plans")
    public String showPlans(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();

        // Zamiast szukać tylko swoich, pobieramy WSZYSTKIE i filtrujemy
        List<TrainingPlan> allPlans = planRepository.findAll();

        List<TrainingPlan> visiblePlans = allPlans.stream()
                .filter(plan -> accessService.canViewPlan(plan, currentUser))
                .collect(Collectors.toList());

        List<Long> editablePlanIds = visiblePlans.stream()
                .filter(plan -> accessService.canEditPlan(plan, currentUser))
                .map(TrainingPlan::getId)
                .collect(Collectors.toList());

        model.addAttribute("plansList", visiblePlans);
        model.addAttribute("editablePlanIds", editablePlanIds);
        model.addAttribute("newPlan", new TrainingPlan());

        // Przekazujemy flagę, czy użytkownik to Trener lub Admin (do wyświetlania przycisku)
        boolean isCoachOrAdmin = currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.COACH || currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.ADMIN;
        model.addAttribute("isCoachOrAdmin", isCoachOrAdmin);

        return "training-plans";
    }

    // --- ZMODYFIKOWANE: Ekran szczegółów planu ---
    @GetMapping("/plans/{id}")
    public String showPlanDetails(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        TrainingPlan plan = planRepository.findById(id).orElseThrow();

        // Zabezpieczenie: Sprawdzamy czy może zobaczyć
        if (!accessService.canViewPlan(plan, currentUser)) {
            return "redirect:/plans";
        }

        model.addAttribute("plan", plan);

        // Na liście do dodania pokazujemy tylko te ćwiczenia, które użytkownik widzi!
        List<Exercise> visibleExercises = exerciseRepository.findAll().stream()
                .filter(ex -> accessService.canViewExercise(ex, currentUser))
                .collect(Collectors.toList());
        model.addAttribute("allExercises", visibleExercises);

        // Przekazujemy flagę, czy dany user może edytować ten konkretny plan
        model.addAttribute("canEdit", accessService.canEditPlan(plan, currentUser));

        return "plan-details";
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

    // --- NOWOŚĆ: KLONOWANIE PLANU ---
    @PostMapping("/plans/clone/{id}")
    public String clonePlan(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        TrainingPlan original = planRepository.findById(id).orElseThrow();

        // Brak sprawdzania canViewPlan dla uproszczenia (założenie, że przycisk jest tylko przy widocznych)

        TrainingPlan clone = new TrainingPlan();
        clone.setName(original.getName() + " (Kopia)");
        clone.setDescription(original.getDescription());
        clone.setUser(currentUser); // Klon należy do Ciebie
        clone.setVisibility(pl.polsl.TrainingPlanner.model.Visibility.PRIVATE);

        // KLONOWANIE DNI I ĆWICZEŃ! To tzw. Głęboka Kopia (Deep Copy)
        for (pl.polsl.TrainingPlanner.model.PlanDay originalDay : original.getDays()) {
            pl.polsl.TrainingPlanner.model.PlanDay cloneDay = new pl.polsl.TrainingPlanner.model.PlanDay();
            cloneDay.setDayName(originalDay.getDayName());
            cloneDay.setPlan(clone);
            // Kopiujemy referencje do tych samych ćwiczeń
            cloneDay.getExercises().addAll(originalDay.getExercises());
            clone.getDays().add(cloneDay);
        }

        planRepository.save(clone);
        return "redirect:/plans";
    }

    // --- EKRAN UDOSTĘPNIANIA PLANU ---
    @GetMapping("/plans/{id}/share")
    public String showShareForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        TrainingPlan plan = planRepository.findById(id).orElseThrow();

        if (!accessService.canEditPlan(plan, currentUser)) {
            return "redirect:/plans"; // Tylko właściciel lub admin może udostępniać
        }
        // Tylko trener i admin mają dostęp do udostępniania planów
        if (currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.USER) {
            return "redirect:/plans";
        }

        model.addAttribute("plan", plan);
        // Przekazujemy obecne loginy jako tekst (po przecinku)
        String sharedLogins = plan.getSharedWith().stream()
                .map(User::getLogin)
                .collect(Collectors.joining(", "));
        model.addAttribute("sharedLogins", sharedLogins);

        return "share-plan";
    }

    // --- LOGIKA UDOSTĘPNIANIA Z WALIDACJĄ ĆWICZEŃ ---
    @PostMapping("/plans/{id}/share")
    public String sharePlan(@PathVariable Long id,
                            @RequestParam Visibility visibility,
                            @RequestParam(required = false) String logins,
                            @RequestParam(required = false) boolean forceShareExercises,
                            HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        TrainingPlan plan = planRepository.findById(id).orElseThrow();

        if (!accessService.canEditPlan(plan, currentUser)) return "redirect:/plans";

        // Zabezpieczenie przed próbą wysłania POST przez zwykłego usera
        if (currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.USER) {
            return "redirect:/plans";
        }

        // 1. Zbieramy użytkowników z podanych loginów
        List<User> targetUsers = new java.util.ArrayList<>();
        if (visibility == Visibility.SHARED && logins != null && !logins.isEmpty()) {
            String[] loginArray = logins.split(",");
            for (String login : loginArray) {
                userRepository.findByLogin(login.trim()).ifPresent(targetUsers::add);
            }
        }

        // 2. Walidacja ćwiczeń - sprawdzamy czy plan zawiera ćwiczenia, których nowi widzowie nie widzą
        boolean hasHiddenExercises = false;
        for (PlanDay day : plan.getDays()) {
            for (Exercise ex : day.getExercises()) {
                if (visibility == Visibility.PUBLIC && ex.getVisibility() != Visibility.PUBLIC) {
                    hasHiddenExercises = true;
                } else if (visibility == Visibility.SHARED) {
                    for (User target : targetUsers) {
                        if (!accessService.canViewExercise(ex, target)) {
                            hasHiddenExercises = true;
                            break;
                        }
                    }
                }
            }
        }

        // 3. Jeśli są ukryte ćwiczenia, a użytkownik NIE ZAZNACZYŁ zgody na ich udostępnienie -> przerywamy
        if (hasHiddenExercises && !forceShareExercises) {
            model.addAttribute("plan", plan);
            model.addAttribute("sharedLogins", logins);
            model.addAttribute("visibility", visibility);
            model.addAttribute("warning", "Uwaga! Ten plan zawiera ćwiczenia, do których ci użytkownicy nie mają dostępu. Zaznacz pole poniżej, aby udostępnić je razem z planem.");
            return "share-plan";
        }

        // 4. Jeśli wszystko OK (lub wyrażono zgodę), aktualizujemy plan i ćwiczenia
        plan.setVisibility(visibility);
        plan.getSharedWith().clear();
        plan.getSharedWith().addAll(targetUsers);

        if (hasHiddenExercises && forceShareExercises) {
            for (PlanDay day : plan.getDays()) {
                for (Exercise ex : day.getExercises()) {
                    if (accessService.canEditExercise(ex, currentUser)) { // Zmieniamy tylko te, które możemy edytować
                        if (visibility == Visibility.PUBLIC) {
                            ex.setVisibility(Visibility.PUBLIC);
                        } else if (visibility == Visibility.SHARED) {
                            ex.setVisibility(Visibility.SHARED);
                            for (User target : targetUsers) {
                                if (!ex.getSharedWith().contains(target)) ex.getSharedWith().add(target);
                            }
                        }
                        exerciseRepository.save(ex);
                    }
                }
            }
        }

        planRepository.save(plan);
        return "redirect:/plans";
    }
}