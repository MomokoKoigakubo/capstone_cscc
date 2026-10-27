package cscc.edu.feedback.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record FeedbackLog(
        Integer feedbackID,
        @NotBlank @Size(max = 1000) String feedbackContent,
        LocalDateTime submitted,
        Integer customerID
) {}