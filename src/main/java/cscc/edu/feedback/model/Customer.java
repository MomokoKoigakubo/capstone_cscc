package cscc.edu.feedback.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

//records work like a standard data class, data final, not setters just getters
public record Customer(
        Integer customerID,
        @NotBlank(message = "Cannot leave First Name blank") @Size(max = 50, message = "Too many characters for First Name") @Pattern(regexp = "^[^<>]*$", message = "No special characters for First Name") String firstName,
        @NotBlank(message = "Cannot leave Last Name blank") @Size(max = 75, message = "Too many characters for Last Name") @Pattern(regexp = "^[^<>]*$", message = "No special characters for Last Name") String lastName,
        @NotBlank(message = "Cannot leave Email blank") @Email(message = "Enter a valid email address") @Size(max = 70, message = "Too many characters for Email") String email
) {}
