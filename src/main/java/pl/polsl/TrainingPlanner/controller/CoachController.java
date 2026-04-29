package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.polsl.TrainingPlanner.model.*;
import pl.polsl.TrainingPlanner.repository.*;

import java.util.List;
import java.util.Optional;

@Controller
public class CoachController {

    private final UserRepository userRepository;
    private final CoachClientRelationRepository relationRepository;
    private final WorkoutLogRepository logRepository;
    private final TrainingPlanRepository planRepository;
    private final PlanEntryRepository planEntryRepository;

    public CoachController(UserRepository userRepository,
                           CoachClientRelationRepository relationRepository,
                           WorkoutLogRepository logRepository,
                           TrainingPlanRepository planRepository,
                           PlanEntryRepository planEntryRepository) {
        this.userRepository = userRepository;
        this.relationRepository = relationRepository;
        this.logRepository = logRepository;
        this.planRepository = planRepository;
        this.planEntryRepository = planEntryRepository;
    }

    @GetMapping("/coach/clients")
    public String showClients(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        User currentUser = userRepository.findById(userId).orElseThrow();
        if (currentUser.getRole() == Role.USER) return "redirect:/dashboard";
        model.addAttribute("user", currentUser);

        List<CoachClientRelation> relations = relationRepository.findByCoachId(userId);
        model.addAttribute("relations", relations);

        // Pobieramy plany trenera, żeby mógł je przypisywać z listy rozwijanej
        List<TrainingPlan> coachPlans = planRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .toList();
        model.addAttribute("coachPlans", coachPlans);

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

        User targetClient = clientOpt.get();

        // NOWE ZABEZPIECZENIE: Sprawdzenie czy zaproszenie już istnieje (oczekujące lub zaakceptowane)
        boolean alreadyInvited = relationRepository.findByCoachId(userId).stream()
                .anyMatch(r -> r.getClient().getId().equals(targetClient.getId()));

        if (alreadyInvited) {
            return "redirect:/coach/clients?error=alreadyexists";
        }

        CoachClientRelation relation = new CoachClientRelation();
        relation.setCoach(coach);
        relation.setClient(targetClient);
        relation.setStatus("PENDING");
        relationRepository.save(relation);

        return "redirect:/coach/clients?success";
    }

    @GetMapping("/coach/clients/{clientId}/workouts")
    public String viewClientWorkouts(@PathVariable Long clientId, HttpSession session, Model model) {
        Long coachId = (Long) session.getAttribute("userId");
        if (coachId == null) return "redirect:/login";

        User coach = userRepository.findById(coachId).orElseThrow();
        if (coach.getRole() == Role.USER) return "redirect:/dashboard";
        model.addAttribute("user", coach);

        boolean isMyClient = relationRepository.findByCoachId(coachId).stream()
                .anyMatch(r -> r.getClient().getId().equals(clientId) && r.getStatus().equals("ACCEPTED"));

        if (!isMyClient) return "redirect:/coach/clients";

        User client = userRepository.findById(clientId).orElseThrow();
        model.addAttribute("client", client);

        List<WorkoutLog> logs = logRepository.findTop5ByUserIdOrderByDateDesc(clientId);
        model.addAttribute("logs", logs);

        return "client-workouts";
    }

    @PostMapping("/coach/clients/{clientId}/assign-plan")
    public String assignPlanToClient(@PathVariable Long clientId, @RequestParam Long planId, HttpSession session) {
        Long coachId = (Long) session.getAttribute("userId");
        if (coachId == null) return "redirect:/login";

        User client = userRepository.findById(clientId).orElseThrow();
        TrainingPlan originalPlan = planRepository.findById(planId).orElseThrow();

        // Klonujemy plan
        TrainingPlan clone = new TrainingPlan();
        clone.setName(originalPlan.getName() + " (From Coach)");
        clone.setDescription(originalPlan.getDescription());
        clone.setUser(client);
        clone.setPublic(false);
        planRepository.save(clone);

        for (PlanEntry originalEntry : originalPlan.getPlanEntries()) {
            PlanEntry cloneEntry = new PlanEntry();
            cloneEntry.setDayOfWeek(originalEntry.getDayOfWeek());
            cloneEntry.setPlan(clone);
            cloneEntry.setExercise(originalEntry.getExercise());
            planEntryRepository.save(cloneEntry);
        }

        client.setCurrentPlanId(clone.getId());
        userRepository.save(client);

        return "redirect:/coach/clients";
    }
}