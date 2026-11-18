package cscc.edu.feedback.model;

import java.util.List;

//one response for the panel, the matching rows and the most used words across them
public record FeedbackSearchResult(
        List<FeedbackLog> feedback,
        List<WordCount> topWords
) {}
