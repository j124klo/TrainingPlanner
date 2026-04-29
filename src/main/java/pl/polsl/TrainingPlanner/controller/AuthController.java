package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.polsl.TrainingPlanner.model.Role;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.repository.UserRepository;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- REJESTRACJA ---
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("newUser", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("newUser") User newUser,
                               @org.springframework.web.bind.annotation.RequestParam(required = false) boolean isCoach,
                               @org.springframework.web.bind.annotation.RequestParam(required = false) String coachPassword,
                               Model model) {

        // 1. Sprawdzenie, czy login jest wolny
        if (userRepository.findByLogin(newUser.getLogin()).isPresent()) {
            model.addAttribute("error", "Login is already taken!");
            return "register";
        }

        // 2. Przypisywanie Ról
        if ("admin".equals(newUser.getLogin())) {
            newUser.setRole(Role.ADMIN); // Magiczny użytkownik admin
        } else if (isCoach) {
            // Weryfikacja hasła dla trenera
            if ("trener123".equals(coachPassword)) {
                newUser.setRole(Role.COACH);
            } else {
                model.addAttribute("error", "Invalid coach verification password!");
                return "register";
            }
        } else {
            newUser.setRole(Role.USER); // Zwykły użytkownik
        }

        // 3. Zapis
        userRepository.save(newUser);
        return "redirect:/login";
    }

    // --- LOGOWANIE ---
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Zwróci plik login.html (błąd 404 zniknie!)
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String login, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> user = userRepository.findByLogin(login);

        // Sprawdzamy czy użytkownik istnieje i czy hasło się zgadza
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            // Zapisujemy w sesji, że ten konkretny użytkownik jest zalogowany
            session.setAttribute("loggedInUser", user.get().getLogin());
            session.setAttribute("userId", user.get().getId());
            return "redirect:/dashboard"; // Przejście do głównego panelu
        } else {
            model.addAttribute("error", "Invalid login or password!");
            return "login";
        }
    }

    // --- WYLOGOWANIE ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Usuwamy sesję
        return "redirect:/login";
    }
}