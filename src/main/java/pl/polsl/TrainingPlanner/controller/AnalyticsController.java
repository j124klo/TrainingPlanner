package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.polsl.TrainingPlanner.model.Role;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.WorkoutLog;
import pl.polsl.TrainingPlanner.repository.CoachClientRelationRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.repository.WorkoutLogRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AnalyticsController {

    private final UserRepository userRepository;
    private final WorkoutLogRepository logRepository;
    private final CoachClientRelationRepository relationRepo;

    public AnalyticsController(UserRepository userRepository, WorkoutLogRepository logRepository, CoachClientRelationRepository relationRepo) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.relationRepo = relationRepo;
    }

    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("user", userRepository.findById(userId).orElseThrow());

        Map<LocalDate, List<WorkoutLog>> history = logRepository.findAll().stream()
                .filter(l -> l.getUser().getId().equals(userId))
                .collect(Collectors.groupingBy(WorkoutLog::getDate, TreeMap::new, Collectors.toList()))
                .descendingMap();

        model.addAttribute("history", history);
        return "history";
    }

    @GetMapping({"/reports", "/coach/clients/{clientId}/reports"})
    public String showReports(@PathVariable(required = false) Long clientId, HttpSession session, Model model) {
        Long loggedInId = (Long) session.getAttribute("userId");
        if (loggedInId == null) return "redirect:/login";
        User loggedInUser = userRepository.findById(loggedInId).orElseThrow();
        model.addAttribute("user", loggedInUser);

        Long targetUserId = loggedInId;

        if (clientId != null && loggedInUser.getRole() == Role.COACH) {
            boolean isMyClient = relationRepo.findByCoachId(loggedInId).stream()
                    .anyMatch(r -> r.getClient().getId().equals(clientId) && r.getStatus().equals("ACCEPTED"));
            if (isMyClient) {
                targetUserId = clientId;
                model.addAttribute("client", userRepository.findById(clientId).orElseThrow());
            }
        }

        // NAPRAWA BŁĘDU LAMBDY: Tworzymy "zamrożoną" kopię zmiennej
        final Long finalTargetUserId = targetUserId;

        List<WorkoutLog> logs = logRepository.findAll().stream()
                .filter(l -> l.getUser().getId().equals(finalTargetUserId))
                .collect(Collectors.toList());

        List<Map<String, Object>> volumeReport = new ArrayList<>();
        Map<String, List<WorkoutLog>> logsByExercise = logs.stream().collect(Collectors.groupingBy(l -> l.getExercise().getName()));

        for (Map.Entry<String, List<WorkoutLog>> entry : logsByExercise.entrySet()) {
            double totalVolume = entry.getValue().stream()
                    .filter(l -> l.getWeight() != null && l.getReps() != null)
                    .mapToDouble(l -> l.getWeight() * l.getReps())
                    .sum();

            long totalSets = entry.getValue().size();

            Map<String, Object> row = new HashMap<>();
            row.put("exerciseName", entry.getKey());
            row.put("totalVolume", totalVolume);
            row.put("totalSets", totalSets);
            volumeReport.add(row);
        }

        Map<LocalDate, Long> exercisesPerDay = logs.stream()
                .collect(Collectors.groupingBy(WorkoutLog::getDate, Collectors.mapping(l -> l.getExercise().getId(), Collectors.toSet())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size(), (a, b) -> a, TreeMap::new));

        long totalTrainingDays = exercisesPerDay.size();
        double avgExercisesPerDay = totalTrainingDays > 0 ? (double) exercisesPerDay.values().stream().mapToLong(Long::longValue).sum() / totalTrainingDays : 0;

        Map<LocalDate, Double> dailyVolume = new TreeMap<>();
        for (WorkoutLog log : logs) {
            if (log.getWeight() != null && log.getReps() != null) {
                dailyVolume.put(log.getDate(), dailyVolume.getOrDefault(log.getDate(), 0.0) + (log.getWeight() * log.getReps()));
            }
        }

        List<String> chartLabels = dailyVolume.keySet().stream().map(LocalDate::toString).collect(Collectors.toList());
        List<Double> chartData = new ArrayList<>(dailyVolume.values());

        model.addAttribute("volumeReport", volumeReport);
        model.addAttribute("totalTrainingDays", totalTrainingDays);
        model.addAttribute("avgExercisesPerDay", Math.round(avgExercisesPerDay * 10.0) / 10.0);
        model.addAttribute("exercisesPerDay", exercisesPerDay);

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        return "reports";
    }
}