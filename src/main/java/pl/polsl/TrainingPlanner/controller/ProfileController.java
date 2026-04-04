package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.polsl.TrainingPlanner.model.BodyMeasurement;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.UserGoal;
import pl.polsl.TrainingPlanner.repository.BodyMeasurementRepository;
import pl.polsl.TrainingPlanner.repository.UserGoalRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final BodyMeasurementRepository measurementRepo;
    private final UserGoalRepository goalRepo;

    public ProfileController(UserRepository userRepository, BodyMeasurementRepository measurementRepo, UserGoalRepository goalRepo) {
        this.userRepository = userRepository;
        this.measurementRepo = measurementRepo;
        this.goalRepo = goalRepo;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        model.addAttribute("user", currentUser);

        // Pobieramy wszystkie pomiary i cele
        List<BodyMeasurement> measurements = measurementRepo.findAll().stream()
                .filter(m -> m.getUser().getId().equals(userId)).toList();
        List<UserGoal> goals = goalRepo.findAll().stream()
                .filter(g -> g.getUser().getId().equals(userId)).toList();

        model.addAttribute("measurements", measurements);
        model.addAttribute("goals", goals);

        return "profile";
    }

    @PostMapping("/profile/measurement")
    public String addMeasurement(@RequestParam Float weight, @RequestParam(required = false) Float bodyFat, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        BodyMeasurement measurement = new BodyMeasurement();
        measurement.setUser(userRepository.findById(userId).orElseThrow());
        measurement.setWeight(weight);
        measurement.setBodyFat(bodyFat);
        measurement.setDate(LocalDate.now());
        measurementRepo.save(measurement);

        return "redirect:/profile";
    }

    @PostMapping("/profile/goal")
    public String addGoal(@RequestParam String description, @RequestParam String deadline, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        UserGoal goal = new UserGoal();
        goal.setUser(userRepository.findById(userId).orElseThrow());
        goal.setDescription(description);
        goal.setDeadline(LocalDate.parse(deadline));
        goalRepo.save(goal);

        return "redirect:/profile";
    }

    @PostMapping("/profile/goal/{id}/check")
    public String toggleGoal(@PathVariable Long id, HttpSession session) {
        UserGoal goal = goalRepo.findById(id).orElseThrow();
        goal.setAchieved(!goal.isAchieved());
        goalRepo.save(goal);
        return "redirect:/profile";
    }
}