package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.WorkoutLog;
import pl.polsl.TrainingPlanner.repository.ExerciseRepository;
import pl.polsl.TrainingPlanner.repository.TrainingPlanRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.repository.WorkoutLogRepository;
import pl.polsl.TrainingPlanner.service.AccessControlService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WorkoutController {

    private final UserRepository userRepository;
    private final TrainingPlanRepository planRepository;
    private final WorkoutLogRepository logRepository;
    private final ExerciseRepository exerciseRepository;
    private final AccessControlService accessService;

    public WorkoutController(UserRepository userRepository, TrainingPlanRepository planRepository, WorkoutLogRepository logRepository, ExerciseRepository exerciseRepository, AccessControlService accessService) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.logRepository = logRepository;
        this.exerciseRepository = exerciseRepository;
        this.accessService = accessService;
    }

    @GetMapping("/workout/start")
    public String startWorkout(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        User currentUser = userRepository.findById(userId).orElseThrow();

        // 1. Zbieramy ćwiczenia zaplanowane na dzisiaj
        Set<Exercise> sessionExercises = new LinkedHashSet<>();
        if (currentUser.getCurrentPlanId() != null) {
            TrainingPlan plan = planRepository.findById(currentUser.getCurrentPlanId()).orElse(null);
            if (plan != null) {
                int today = LocalDate.now().getDayOfWeek().getValue();
                plan.getPlanEntries().stream()
                        .filter(entry -> entry.getDayOfWeek() == today)
                        .forEach(entry -> sessionExercises.add(entry.getExercise()));
            }
        }

        // 2. Pobieramy wszystkie dzisiejsze zrobione już logi
        List<WorkoutLog> todaysLogs = logRepository.findAll().stream()
                .filter(log -> log.getUser().getId().equals(userId) && log.getDate().equals(LocalDate.now()))
                .collect(Collectors.toList());

        // 3. Dodajemy do listy ćwiczeń te spoza planu (jeśli ktoś zaczął je robić)
        todaysLogs.forEach(log -> sessionExercises.add(log.getExercise()));

        // Grupowanie logów po ID ćwiczenia, by łatwo wyświetlić je w tabelkach
        Map<Long, List<WorkoutLog>> logsByExercise = todaysLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getExercise().getId()));

        model.addAttribute("sessionExercises", sessionExercises);
        model.addAttribute("logsByExercise", logsByExercise);

        // 4. Lista wszystkich widocznych ćwiczeń do wyboru (do "ćwiczeń ekstra")
        List<Exercise> allExercises = exerciseRepository.findAll().stream()
                .filter(ex -> accessService.canViewExercise(ex, currentUser))
                .collect(Collectors.toList());
        model.addAttribute("allExercises", allExercises);

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
        log.setSetNumber(setNumber); // Zapisujemy numer serii z formularza
        log.setWeight(weight);
        log.setReps(reps);

        logRepository.save(log);
        return "redirect:/workout/start"; // Odświeża stronę pokazując nowy wpis
    }
}