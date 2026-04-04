package pl.polsl.TrainingPlanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.repository.UserRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Ten kod wykonuje się przed każdym załadowaniem strony HTML!
    // Automatycznie przekazuje "user" do menu bocznego.
    @ModelAttribute("user")
    public User globalUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null; // Zwraca null, czyli GOŚĆ
    }
}