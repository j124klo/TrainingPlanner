package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Zabezpieczenie przed niezalogowanymi użytkownikami
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        // Przekazujemy login do pliku HTML, żeby wyświetlić "Witaj, [Login]!"
        model.addAttribute("username", session.getAttribute("loggedInUser"));
        return "dashboard";
    }
}