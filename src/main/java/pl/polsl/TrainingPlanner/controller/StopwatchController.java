package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.polsl.TrainingPlanner.repository.UserRepository;

@Controller
public class StopwatchController {
    private final UserRepository userRepository;
    public StopwatchController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping("/stopwatch")
    public String showStopwatch(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("user", userRepository.findById(userId).orElseThrow());
        }
        return "stopwatch";
    }
}