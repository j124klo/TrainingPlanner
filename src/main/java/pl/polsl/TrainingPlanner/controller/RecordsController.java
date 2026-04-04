package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.WorkoutLog;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.repository.WorkoutLogRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RecordsController {

    private final UserRepository userRepository;
    private final WorkoutLogRepository logRepository;

    public RecordsController(UserRepository userRepository, WorkoutLogRepository logRepository) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
    }

    @GetMapping("/records")
    public String showRecords(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("user", userRepository.findById(userId).orElseThrow());

        List<WorkoutLog> allLogs = logRepository.findAll().stream()
                .filter(l -> l.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        // MAX STRENGTH (Największy ciężar dla każdego ćwiczenia)
        Map<Exercise, WorkoutLog> maxStrength = allLogs.stream()
                .filter(l -> l.getWeight() != null && l.getWeight() > 0)
                .collect(Collectors.toMap(WorkoutLog::getExercise, log -> log,
                        (log1, log2) -> log1.getWeight() >= log2.getWeight() ? log1 : log2));

        // MAX ENDURANCE (Najwięcej powtórzeń w ogóle)
        Map<Exercise, WorkoutLog> maxEndurance = allLogs.stream()
                .filter(l -> l.getReps() != null && l.getReps() > 0)
                .collect(Collectors.toMap(WorkoutLog::getExercise, log -> log,
                        (log1, log2) -> log1.getReps() >= log2.getReps() ? log1 : log2));

        // MAX TIME (Najdłuższy czas)
        Map<Exercise, WorkoutLog> maxTime = allLogs.stream()
                .filter(l -> l.getTimeMinutes() != null && l.getTimeMinutes() > 0)
                .collect(Collectors.toMap(WorkoutLog::getExercise, log -> log,
                        (log1, log2) -> log1.getTimeMinutes() >= log2.getTimeMinutes() ? log1 : log2));

        // MAX DISTANCE (Największy dystans)
        Map<Exercise, WorkoutLog> maxDistance = allLogs.stream()
                .filter(l -> l.getDistance() != null && l.getDistance() > 0)
                .collect(Collectors.toMap(WorkoutLog::getExercise, log -> log,
                        (log1, log2) -> log1.getDistance() >= log2.getDistance() ? log1 : log2));

        model.addAttribute("maxStrength", maxStrength);
        model.addAttribute("maxEndurance", maxEndurance);
        model.addAttribute("maxTime", maxTime);
        model.addAttribute("maxDistance", maxDistance);

        return "records";
    }
}