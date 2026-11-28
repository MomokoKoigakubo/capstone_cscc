package cscc.edu.feedback;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FeedbackCSVTests {
    @Test
    void commaGetsWrappedInQuotes() {
        assertEquals("\"a,b\"", FeedbackCSV.escape("a,b"));
    }

    @Test
    void quotesGetDoubled() {
        assertEquals("\"She said \"\"hi\"\"\"", FeedbackCSV.escape("She said \"hi\""));
    }

    @Test
    void csvSanitize() {
        assertEquals("'=SUM(A1)", FeedbackCSV.escape("=SUM(A1)"));
    }

    @Test
    void CtrlGroup() {
        assertEquals("Hello World!", FeedbackCSV.escape("Hello World!"));
    }
}
