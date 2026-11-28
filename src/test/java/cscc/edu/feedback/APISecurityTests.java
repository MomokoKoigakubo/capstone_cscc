package cscc.edu.feedback;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cscc.edu.feedback.repository.FeedbackRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=unused",
        "spring.datasource.username=unused",
        "spring.datasource.password=unused",
        "spring.security.user.name=test_admin",
        "spring.security.user.password=test_password",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class ApiSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedbackRepository feedbackRepository;

    @Test
    void feedbackListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/feedback"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanViewFeedback() throws Exception {
        mockMvc.perform(get("/api/feedback")
                        .with(httpBasic("test_admin", "test_password")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsCustomerWithEmptyEmail() throws Exception {
        String invalidCustomer = """
                {
                    "firstName": "John",
                    "lastName": "Smith",
                    "email": ""
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .with(csrf())
                        .with(httpBasic("test_admin", "test_password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCustomer))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyFeedback() throws Exception {
        String invalidFeedback = """
                {
                    "feedbackContent": "",
                    "customerID": 1
                }
                """;

        mockMvc.perform(post("/api/feedback")
                        .with(csrf())
                        .with(httpBasic("test_admin", "test_password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidFeedback))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerCanSubmitFeedbackWithoutLogin() throws Exception {
        String invalidFeedback = """
                {
                    "feedbackContent": "",
                    "customerID": 1
                }
                """;

        mockMvc.perform(post("/api/feedback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidFeedback))
                .andExpect(status().isBadRequest());
    }

    @Test
    void incorrectAdminPasswordIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback")
                        .with(httpBasic("test_admin", "wrong_password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsFeedbackOver1000Characters() throws Exception {
        String longFeedback = "A".repeat(1001);

        String json = """
                {
                    "feedbackContent": "%s",
                    "customerID": 1
                }
                """.formatted(longFeedback);

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousUserCannotAccessCustomerEndpoint() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
