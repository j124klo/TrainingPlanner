package pl.polsl.TrainingPlanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.polsl.TrainingPlanner.repository.UserRepository;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public String showUsers(Model model) {
        // Pobieramy wszystkich użytkowników z bazy
        model.addAttribute("usersList", userRepository.findAll());
        // Zwracamy nazwę pliku HTML (bez rozszerzenia), który ma się wyświetlić
        return "users-page";
    }
}