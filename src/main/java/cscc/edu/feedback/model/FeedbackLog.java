package cscc.edu.feedback.model;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record FeedbackLog(
        Integer feedbackID,
        @NotBlank @Size(max = 1000) String feedbackContent,
        LocalDateTime submitted,
        Integer customerID,
        @NotNull @Max(5) @Min(1) Integer rating
        ) {}