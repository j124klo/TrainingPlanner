package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.polsl.TrainingPlanner.model.CoachClientRelation;
import pl.polsl.TrainingPlanner.model.Role;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.WorkoutLog;
import pl.polsl.TrainingPlanner.repository.CoachClientRelationRepository;
import pl.polsl.TrainingPlanner.repository.UserRepository;
import pl.polsl.TrainingPlanner.repository.WorkoutLogRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class CoachController {

    private final UserRepository userRepository;
    private final CoachClientRelationRepository relationRepository;
    private final WorkoutLogRepository logRepository; // DODANE!

    public CoachController(UserRepository userRepository, CoachClientRelationRepository relationRepository, WorkoutLogRepository logRepository) {
        this.userRepository = userRepository;
        this.relationRepository = relationRepository;
        this.logRepository = logRepository;
    }

    @GetMapping("/coach/clients")
    public String showClients(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        if (currentUser.getRole() == Role.USER) return "redirect:/dashboard";
        model.addAttribute("user", currentUser); // Wymagane dla fragments.html

        List<CoachClientRelation> relations = relationRepository.findByCoachId(userId);
        model.addAttribute("relations", relations);

        return "coach-clients";
    }

    @PostMapping("/coach/invite")
    public String inviteClient(@RequestParam String login, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        User coach = userRepository.findById(userId).orElseThrow();

        Optional<User> clientOpt = userRepository.findByLogin(login);
        if (clientOpt.isEmpty()) {
            return "redirect:/coach/clients?error=notfound";
        }

        CoachClientRelation relation = new CoachClientRelation();
        relation.setCoach(coach);
        relation.setClient(clientOpt.get());
        relation.setStatus("PENDING");
        relationRepository.save(relation);

        return "redirect:/coach/clients?success";
    }

    // --- NOWOŚĆ: PODGLĄD DZIENNIKA KLIENTA ---
    @GetMapping("/coach/clients/{clientId}/workouts")
    public String viewClientWorkouts(@PathVariable Long clientId, HttpSession session, Model model) {
        Long coachId = (Long) session.getAttribute("userId");
        if (coachId == null) return "redirect:/login";

        User coach = userRepository.findById(coachId).orElseThrow();
        if (coach.getRole() == Role.USER) return "redirect:/dashboard";
        model.addAttribute("user", coach);

        // Zabezpieczenie: Sprawdzamy, czy ten klient faktycznie przypisany jest do tego trenera
        boolean isMyClient = relationRepository.findByCoachId(coachId).stream()
                .anyMatch(r -> r.getClient().getId().equals(clientId) && r.getStatus().equals("ACCEPTED"));

        if (!isMyClient) return "redirect:/coach/clients";

        User client = userRepository.findById(clientId).orElseThrow();
        model.addAttribute("client", client);

        // Wyciągamy ostatnie logi treningowe klienta
        List<WorkoutLog> logs = logRepository.findTop5ByUserIdOrderByDateDesc(clientId);
        model.addAttribute("logs", logs);

        return "client-workouts";
    }
}