package cscc.edu.feedback.model;

import java.time.LocalDate;

public record TrendPoint(LocalDate month, long totalReviews, double avgRating) {}
