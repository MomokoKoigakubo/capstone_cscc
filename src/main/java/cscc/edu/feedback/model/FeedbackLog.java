package cscc.edu.feedback.model;

import java.time.LocalDateTime;

public record FeedbackLog(Integer feedbackID, String feedbackContent, LocalDateTime submitted, Integer customerID) {
}