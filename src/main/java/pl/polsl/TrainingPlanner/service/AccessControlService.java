package pl.polsl.TrainingPlanner.service;

import org.springframework.stereotype.Service;
import pl.polsl.TrainingPlanner.model.Exercise;
import pl.polsl.TrainingPlanner.model.Role;
import pl.polsl.TrainingPlanner.model.TrainingPlan;
import pl.polsl.TrainingPlanner.model.User;

@Service
public class AccessControlService {

    public boolean canViewPlan(TrainingPlan plan, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        if (plan.getUser() != null && plan.getUser().getId().equals(user.getId())) return true;
        return plan.isPublic(); // Zwraca true jeśli plan jest publiczny
    }

    public boolean canEditPlan(TrainingPlan plan, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        return plan.getUser() != null && plan.getUser().getId().equals(user.getId());
    }

    public boolean canViewExercise(Exercise exercise, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        if (exercise.getOwner() != null && exercise.getOwner().getId().equals(user.getId())) return true;
        return exercise.isPublic(); // Zwraca true jeśli ćwiczenie jest publiczne
    }

    public boolean canEditExercise(Exercise exercise, User user) {
        if (user.getRole() == Role.ADMIN) return true;
        return exercise.getOwner() != null && exercise.getOwner().getId().equals(user.getId());
    }
}