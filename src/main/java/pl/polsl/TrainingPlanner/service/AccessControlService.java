package pl.polsl.TrainingPlanner.service;

import org.springframework.stereotype.Service;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.Role;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;
import pl.polsl.TrainingPlanner.model.Visibility;

@Service
public class AccessControlService {

    public boolean canViewPlan(TrainingPlan plan, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        // Dodane sprawdzanie czy user nie jest nullem
        if (plan.getUser() != null && plan.getUser().getId().equals(user.getId())) return true;
        // Dodane sprawdzanie czy visibility nie jest nullem
        if (plan.getVisibility() != null && plan.getVisibility() == Visibility.PUBLIC) return true;
        if (plan.getVisibility() != null && plan.getVisibility() == Visibility.SHARED && plan.getSharedWith() != null && plan.getSharedWith().contains(user)) return true;
        return false;
    }

    public boolean canEditPlan(TrainingPlan plan, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        return plan.getUser() != null && plan.getUser().getId().equals(user.getId());
    }

    public boolean canViewExercise(Exercise exercise, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        if (exercise.getOwner() != null && exercise.getOwner().getId().equals(user.getId())) return true;
        if (exercise.getVisibility() != null && exercise.getVisibility() == Visibility.PUBLIC) return true;
        if (exercise.getVisibility() != null && exercise.getVisibility() == Visibility.SHARED && exercise.getSharedWith() != null && exercise.getSharedWith().contains(user)) return true;
        return false;
    }

    public boolean canEditExercise(Exercise exercise, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        return exercise.getOwner() != null && exercise.getOwner().getId().equals(user.getId());
    }
}