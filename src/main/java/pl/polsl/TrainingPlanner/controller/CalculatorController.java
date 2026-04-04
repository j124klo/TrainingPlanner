package pl.polsl.TrainingPlanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CalculatorController {

    @GetMapping("/calculator")
    public String showCalculator() {
        return "calculator";
    }

    @PostMapping("/calculator")
    public String calculate1RM(@RequestParam double weight, @RequestParam int reps, Model model) {

        // Zamiast Epleya używamy współczynników dokładnie z tabeli (standard NSCA / Brzycki)
        double coefficient;
        switch (reps) {
            case 1: coefficient = 1.0; break;
            case 2: coefficient = 0.95; break;
            case 4: coefficient = 0.90; break;
            case 6: coefficient = 0.85; break;
            case 8: coefficient = 0.80; break;
            case 10: coefficient = 0.75; break;
            case 12: coefficient = 0.70; break;
            case 16: coefficient = 0.65; break;
            case 20: coefficient = 0.60; break;
            case 24: coefficient = 0.55; break;
            case 30: coefficient = 0.50; break;
            default:
                // Dla nietypowych powtórzeń (np. 3, 5, 7) klasyczny wzór Brzyckiego
                coefficient = (37.0 - reps) / 36.0;
                break;
        }

        // Wyliczamy 1RM tak, żeby idealnie zgadzał się z tabelą
        double oneRm = weight / coefficient;
        double rounded1Rm = Math.round(oneRm * 10.0) / 10.0;

        int[] percentages = {100, 95, 90, 85, 80, 75, 70, 65, 60, 55, 50};
        int[] estimatedReps = {1, 2, 4, 6, 8, 10, 12, 16, 20, 24, 30};

        java.util.List<java.util.Map<String, Object>> tableData = new java.util.ArrayList<>();

        for (int i = 0; i < percentages.length; i++) {
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            row.put("percent", percentages[i] + "%");

            double calcWeight = rounded1Rm * (percentages[i] / 100.0);
            row.put("weight", Math.round(calcWeight * 10.0) / 10.0);
            row.put("reps", estimatedReps[i]);
            tableData.add(row);
        }

        model.addAttribute("weight", weight);
        model.addAttribute("reps", reps);
        model.addAttribute("result", rounded1Rm);
        model.addAttribute("tableData", tableData);

        return "calculator";
    }
}