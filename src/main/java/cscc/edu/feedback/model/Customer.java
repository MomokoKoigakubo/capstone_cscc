package cscc.edu.feedback.model;

//records work like a standard data class, data final, not setters just getters
public record Customer(Integer customerID, String firstName, String lastName, String email) {}
