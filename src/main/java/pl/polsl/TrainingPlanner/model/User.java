package pl.polsl.TrainingPlanner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // Nazwa tabeli w bazie
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // To oznacza SERIAL (auto-inkrementację) w PostgreSQL
    private Long id;

    private String login;
    private String password;

    // Pamiętajcie o wygenerowaniu Getterów, Setterów i bezparametrowego konstruktora!
}