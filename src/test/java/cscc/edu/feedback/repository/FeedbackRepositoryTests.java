package cscc.edu.feedback.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedbackRepositoryTests {

    @Test
    void likeWildcardsAreEscaped() {
        assertEquals("100\\%", FeedbackRepository.escapeLike("100%"));
        assertEquals("a\\_b", FeedbackRepository.escapeLike("a_b"));
        assertEquals("\\[x]", FeedbackRepository.escapeLike("[x]"));
        assertEquals("c:\\\\temp", FeedbackRepository.escapeLike("c:\\temp"));
    }

    @Test
    void plainTextIsUnchanged() {
        assertEquals("late delivery", FeedbackRepository.escapeLike("late delivery"));
    }
}
