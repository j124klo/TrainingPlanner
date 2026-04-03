package pl.polsl.TrainingPlanner.model;

public enum Visibility {
    PRIVATE,  // Tylko dla właściciela (i admina)
    SHARED,   // Udostępnione konkretnym osobom (po loginie)
    PUBLIC    // Widoczne dla wszystkich
}