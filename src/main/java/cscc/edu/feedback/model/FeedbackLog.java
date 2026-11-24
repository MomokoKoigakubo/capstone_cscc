package cscc.edu.feedback.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FeedbackLog(
        Integer feedbackID,
        @NotBlank @Size(max = 1000) String feedbackContent,
        LocalDateTime submitted,
        Integer customerID,
        @NotNull @Min(1) @Max(5) Integer rating
) {}