package cscc.edu.feedback;

import cscc.edu.feedback.config.SecurityConfig;
import cscc.edu.feedback.controller.FeedbackController;
import cscc.edu.feedback.model.FeedbackLog;
import cscc.edu.feedback.model.SortOrder;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackController.class)
@Import(SecurityConfig.class)
class FeedbackSearchTests {

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
    void anonymousSearchIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback/search").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void searchWithNoFiltersReturnsEverythingNewestFirst() throws Exception {
        when(repository.searchFeedback(isNull(), isNull(), isNull(), eq(SortOrder.NEWEST)))
                .thenReturn(List.of(
                        new FeedbackLog(2, "Delivery was late, delivery driver was rude", LocalDateTime.now(), 1, 2),
                        new FeedbackLog(1, "Late delivery again", LocalDateTime.now(), 1, 3)));

        mockMvc.perform(get("/api/feedback/search").with(owner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedback.length()").value(2))
                .andExpect(jsonPath("$.topWords[0].word").value("delivery"))
                .andExpect(jsonPath("$.topWords[0].count").value(3))
                .andExpect(jsonPath("$.topWords[1].word").value("late"));
    }

    @Test
    void filtersArePassedThroughToTheRepository() throws Exception {
        when(repository.searchFeedback(any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/feedback/search").with(owner())
                        .param("keyword", "late")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-18")
                        .param("sort", "oldest"))
                .andExpect(status().isOk());

        verify(repository).searchFeedback("late", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 18), SortOrder.OLDEST);
    }

    @Test
    void injectionTextIsTreatedAsAKeyword() throws Exception {
        String attack = "'; DROP TABLE dbo.feedbacklog; --";
        when(repository.searchFeedback(any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/feedback/search").with(owner()).param("keyword", attack))
                .andExpect(status().isOk());

        //handed to the repository as a value, which binds it as a parameter
        verify(repository).searchFeedback(eq(attack), isNull(), isNull(), eq(SortOrder.NEWEST));
    }

    @Test
    void nonIsoDateIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback/search").with(owner()).param("from", "11/18/2026"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void fromAfterToIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback/search").with(owner())
                        .param("from", "2026-11-18")
                        .param("to", "2026-11-01"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void unknownSortIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback/search").with(owner()).param("sort", "feedbackID; DROP TABLE x"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void overlongKeywordIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback/search").with(owner()).param("keyword", "a".repeat(101)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
