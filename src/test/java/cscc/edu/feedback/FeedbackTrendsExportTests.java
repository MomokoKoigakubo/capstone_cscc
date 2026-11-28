package cscc.edu.feedback;

import cscc.edu.feedback.config.SecurityConfig;
import cscc.edu.feedback.controller.FeedbackController;
import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.model.TrendPoint;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedbackController.class)
@Import(SecurityConfig.class)
public class FeedbackTrendsExportTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FeedbackRepository repository;

    @MockitoBean
    SecurityEventLogger securityEventLogger;

    private static org.springframework.test.web.servlet.request.RequestPostProcessor owner() {
        return user("owner");
    }

    @Test
    void trendsReturnMonthlyPoints() throws Exception {
        when(repository.trendsByMonth())
                .thenReturn(List.of(new TrendPoint(LocalDate.of(2015, 3, 1), 10, 4.5)));

        mockMvc.perform(get("/api/feedback/trends").with(owner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalReviews").value(10));
    }

    @Test
    void exportReturnsCsvDownload() throws Exception {
        when(repository.searchFeedback(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new FeedbackLog(1, "Great game, loved it", LocalDateTime.now(), 1, 5)));

        mockMvc.perform(get("/api/feedback/export").with(owner()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string(containsString("\"Great game, loved it\"")));
    }

    @Test
    void anonymousExportIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback/export").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void ratingOutOfRange() throws Exception {
        mockMvc.perform(get("/api/feedback/search").with(owner()).param("keyword", "a".repeat(101)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
