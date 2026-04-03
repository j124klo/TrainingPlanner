package pl.polsl.TrainingPlanner.service;

import org.springframework.stereotype.Service;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.Role;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.Visibility;

@Service
public class AccessControlService {

    // --- CZY UŻYTKOWNIK MOŻE ZOBACZYĆ PLAN? ---
    public boolean canViewPlan(TrainingPlan plan, User user) {
        if (user.getRole() == Role.ADMIN) return true; // Admin widzi wszystko
        if (plan.getUser().getId().equals(user.getId())) return true; // Właściciel widzi swoje
        if (plan.getVisibility() == Visibility.PUBLIC) return true; // Wszyscy widzą publiczne
        if (plan.getVisibility() == Visibility.SHARED && plan.getSharedWith().contains(user)) return true; // Zaproszeni widzą udostępnione
        return false;
    }

    // --- CZY UŻYTKOWNIK MOŻE EDYTOWAĆ PLAN? ---
    public boolean canEditPlan(TrainingPlan plan, User user) {
        if (user.getRole() == Role.ADMIN) return true; // Admin edytuje wszystko
        return plan.getUser().getId().equals(user.getId()); // Tylko właściciel edytuje resztę
    }

    // --- CZY UŻYTKOWNIK MOŻE ZOBACZYĆ ĆWICZENIE? ---
    public boolean canViewExercise(Exercise exercise, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        if (exercise.getOwner() != null && exercise.getOwner().getId().equals(user.getId())) return true;
        if (exercise.getVisibility() == Visibility.PUBLIC) return true;
        if (exercise.getVisibility() == Visibility.SHARED && exercise.getSharedWith().contains(user)) return true;
        return false;
    }

    // --- CZY UŻYTKOWNIK MOŻE EDYTOWAĆ ĆWICZENIE? ---
    public boolean canEditExercise(Exercise exercise, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        return exercise.getOwner() != null && exercise.getOwner().getId().equals(user.getId());
    }
}