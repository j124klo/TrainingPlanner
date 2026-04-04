package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.polsl.TrainingPlanner.model.PlanEntry;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.WorkoutLog;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.TrainingPlanRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.repository.WorkoutLogRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WorkoutController {

    private final UserRepository userRepository;
    private final TrainingPlanRepository planRepository;
    private final WorkoutLogRepository logRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutController(UserRepository userRepository, TrainingPlanRepository planRepository, WorkoutLogRepository logRepository, ExerciseRepository exerciseRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.logRepository = logRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @GetMapping("/workout/start")
    public String startWorkout(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        if (currentUser.getCurrentPlanId() == null) {
            return "redirect:/dashboard"; // Brak planu, wracamy
        }

        TrainingPlan plan = planRepository.findById(currentUser.getCurrentPlanId()).orElseThrow();
        int today = LocalDate.now().getDayOfWeek().getValue();

        List<PlanEntry> todaysEntries = plan.getPlanEntries().stream()
                .filter(entry -> entry.getDayOfWeek() == today)
                .collect(Collectors.toList());

        model.addAttribute("todaysEntries", todaysEntries);
        return "workout-session";
    }

    @PostMapping("/workout/log")
    public String logExercise(@RequestParam Long exerciseId, @RequestParam Float weight, @RequestParam Integer reps, @RequestParam Integer setNumber, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        WorkoutLog log = new WorkoutLog();
        log.setUser(userRepository.findById(userId).orElseThrow());
        log.setExercise(exerciseRepository.findById(exerciseId).orElseThrow());
        log.setDate(LocalDate.now());
        log.setSetNumber(setNumber);
        log.setWeight(weight);
        log.setReps(reps);

        logRepository.save(log);
        return "redirect:/workout/start"; // Zostajemy na ekranie treningu
    }
}