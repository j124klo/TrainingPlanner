package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.polsl.TrainingPlanner.model.PlanEntry;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.WorkoutLog; // IMPORT NOWY
import pl.polsl.TrainingPlanner.repository.TrainingPlanRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.repository.WorkoutLogRepository; // IMPORT NOWY

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final TrainingPlanRepository planRepository;
    private final WorkoutLogRepository workoutLogRepository; // NOWE REPOZYTORIUM

    public DashboardController(UserRepository userRepository, TrainingPlanRepository planRepository, WorkoutLogRepository workoutLogRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.workoutLogRepository = workoutLogRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        model.addAttribute("user", currentUser);

        // --- 1. SEKCJA KALENDARZA ---
        if (currentUser.getCurrentPlanId() != null) {
            TrainingPlan activePlan = planRepository.findById(currentUser.getCurrentPlanId()).orElse(null);
            if (activePlan != null) {
                model.addAttribute("activePlan", activePlan);

                Map<Integer, List<PlanEntry>> weeklyPlan = new HashMap<>();
                for (int i = 1; i <= 7; i++) {
                    weeklyPlan.put(i, new ArrayList<>());
                }

                for (PlanEntry entry : activePlan.getPlanEntries()) {
                    weeklyPlan.get(entry.getDayOfWeek()).add(entry);
                }
                model.addAttribute("weeklyPlan", weeklyPlan);
            }
        }

        // --- 2. SEKCJA LOGÓW (REKORDY) ---
        List<WorkoutLog> recentLogs = workoutLogRepository.findTop5ByUserIdOrderByDateDesc(currentUser.getId());
        model.addAttribute("recentLogs", recentLogs);

        return "dashboard";
    }
}