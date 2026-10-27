package cscc.edu.feedback.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//records work like a standard data class, data final, not setters just getters
public record Customer(
        Integer customerID,
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 75) String lastName,
        @NotBlank @Email @Size(max = 70) String email
) {}
