package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.PlanEntry;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.Visibility;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.PlanEntryRepository;
import pl.polsl.TrainingPlanner.repository.TrainingPlanRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.service.AccessControlService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TrainingPlanController {

    private final TrainingPlanRepository planRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final PlanEntryRepository planEntryRepository;
    private final AccessControlService accessService;

    public TrainingPlanController(TrainingPlanRepository planRepository, UserRepository userRepository, ExerciseRepository exerciseRepository, PlanEntryRepository planEntryRepository, AccessControlService accessService) {
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.planEntryRepository = planEntryRepository;
        this.accessService = accessService;
    }

    // 1. WYŚWIETLANIE LISTY PLANÓW
    @GetMapping("/plans")
    public String showPlans(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
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

        boolean isCoachOrAdmin = currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.COACH || currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.ADMIN;
        model.addAttribute("isCoachOrAdmin", isCoachOrAdmin);

        return "training-plans";
    }

    // 2. SZCZEGÓŁY PLANU
    @GetMapping("/plans/{id}")
    public String showPlanDetails(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        TrainingPlan plan = planRepository.findById(id).orElseThrow();

        if (!accessService.canViewPlan(plan, currentUser)) {
            return "redirect:/plans";
        }

        model.addAttribute("plan", plan);

        List<Exercise> visibleExercises = exerciseRepository.findAll().stream()
                .filter(ex -> accessService.canViewExercise(ex, currentUser))
                .collect(Collectors.toList());
        model.addAttribute("allExercises", visibleExercises);
        model.addAttribute("canEdit", accessService.canEditPlan(plan, currentUser));

        String[] dayNames = {"", "Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        model.addAttribute("dayNames", dayNames);

        return "plan-details";
    }

    // 3. DODAWANIE PLANU
    @PostMapping("/plans/add")
    public String addPlan(@ModelAttribute TrainingPlan newPlan, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        newPlan.setUser(currentUser);
        newPlan.setVisibility(Visibility.PRIVATE);

        planRepository.save(newPlan);
        return "redirect:/plans";
    }

    // 4. USUWANIE PLANU
    @PostMapping("/plans/delete/{id}")
    public String deletePlan(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        User currentUser = userRepository.findById(userId).orElseThrow();
        TrainingPlan plan = planRepository.findById(id).orElseThrow();

        if (accessService.canEditPlan(plan, currentUser)) {
            planRepository.deleteById(id);
        }
        return "redirect:/plans";
    }

    // 5. DODAWANIE ĆWICZENIA DO PLANU (PlanEntry)
    @PostMapping("/plans/{planId}/addExercise")
    public String addExerciseToPlan(@PathVariable Long planId, @RequestParam Long exerciseId, @RequestParam Integer dayOfWeek, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        TrainingPlan plan = planRepository.findById(planId).orElseThrow();
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();

        PlanEntry entry = new PlanEntry();
        entry.setPlan(plan);
        entry.setExercise(exercise);
        entry.setDayOfWeek(dayOfWeek);

        planEntryRepository.save(entry);
        return "redirect:/plans/" + planId;
    }

    // 6. USUWANIE ĆWICZENIA Z PLANU (PlanEntry)
    @PostMapping("/plans/{planId}/removeExercise/{entryId}")
    public String removeExerciseFromPlan(@PathVariable Long planId, @PathVariable Long entryId, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        planEntryRepository.deleteById(entryId);
        return "redirect:/plans/" + planId;
    }

    // 7. KLONOWANIE PLANU
    @PostMapping("/plans/clone/{id}")
    public String clonePlan(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        TrainingPlan original = planRepository.findById(id).orElseThrow();

        TrainingPlan clone = new TrainingPlan();
        clone.setName(original.getName() + " (Kopia)");
        clone.setDescription(original.getDescription());
        clone.setUser(currentUser);
        clone.setVisibility(Visibility.PRIVATE);

        // Zmodyfikowane: Kopiowanie po nowemu (PlanEntry)
        for (PlanEntry originalEntry : original.getPlanEntries()) {
            PlanEntry cloneEntry = new PlanEntry();
            cloneEntry.setDayOfWeek(originalEntry.getDayOfWeek());
            cloneEntry.setPlan(clone);
            cloneEntry.setExercise(originalEntry.getExercise());
            clone.getPlanEntries().add(cloneEntry);
        }

        planRepository.save(clone);
        return "redirect:/plans";
    }

    // 8. EKRAN UDOSTĘPNIANIA PLANU
    @GetMapping("/plans/{id}/share")
    public String showShareForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        if (currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.USER) return "redirect:/plans";

        TrainingPlan plan = planRepository.findById(id).orElseThrow();
        if (!accessService.canEditPlan(plan, currentUser)) return "redirect:/plans";

        model.addAttribute("plan", plan);
        String sharedLogins = plan.getSharedWith().stream()
                .map(User::getLogin)
                .collect(Collectors.joining(", "));
        model.addAttribute("sharedLogins", sharedLogins);

        return "share-plan";
    }

    // 9. LOGIKA UDOSTĘPNIANIA Z WALIDACJĄ
    @PostMapping("/plans/{id}/share")
    public String sharePlan(@PathVariable Long id,
                            @RequestParam Visibility visibility,
                            @RequestParam(required = false) String logins,
                            @RequestParam(required = false) boolean forceShareExercises,
                            HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        if (currentUser.getRole() == pl.polsl.TrainingPlanner.model.Role.USER) return "redirect:/plans";

        TrainingPlan plan = planRepository.findById(id).orElseThrow();
        if (!accessService.canEditPlan(plan, currentUser)) return "redirect:/plans";

        List<User> targetUsers = new ArrayList<>();
        if (visibility == Visibility.SHARED && logins != null && !logins.isEmpty()) {
            String[] loginArray = logins.split(",");
            for (String login : loginArray) {
                userRepository.findByLogin(login.trim()).ifPresent(targetUsers::add);
            }
        }

        boolean hasHiddenExercises = false;
        // Zmodyfikowane: Sprawdzanie ukrytych ćwiczeń po nowemu (PlanEntry)
        for (PlanEntry entry : plan.getPlanEntries()) {
            Exercise ex = entry.getExercise();
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

        if (hasHiddenExercises && !forceShareExercises) {
            model.addAttribute("plan", plan);
            model.addAttribute("sharedLogins", logins);
            model.addAttribute("visibility", visibility);
            model.addAttribute("warning", "Uwaga! Ten plan zawiera ćwiczenia, do których ci użytkownicy nie mają dostępu. Zaznacz pole poniżej, aby udostępnić je razem z planem.");
            return "share-plan";
        }

        plan.setVisibility(visibility);
        plan.getSharedWith().clear();
        plan.getSharedWith().addAll(targetUsers);

        if (hasHiddenExercises && forceShareExercises) {
            for (PlanEntry entry : plan.getPlanEntries()) {
                Exercise ex = entry.getExercise();
                if (accessService.canEditExercise(ex, currentUser)) {
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

        planRepository.save(plan);
        return "redirect:/plans";
    }
}