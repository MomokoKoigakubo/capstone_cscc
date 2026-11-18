package cscc.edu.feedback;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.verify;
import cscc.edu.feedback.config.SecurityConfig;
import cscc.edu.feedback.controller.CustomerController;
import cscc.edu.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import cscc.edu.feedback.SecurityEventLogger;
import org.springframework.beans.factory.annotation.Qualifier;
@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
class SecurityConfigTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    @Qualifier("filterChain")
    SecurityFilterChain chain;

    @MockitoBean
    FeedbackRepository repository;

    @MockitoBean
    SecurityEventLogger securityEventLogger;

    @Test
    void anonymousCustomerPostIsAllowedWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Roe\",\"email\":\"janeroe@gmail.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCustomerGetIsRejected() throws Exception {
        //the owner panel fetches json, so an expired session surfaces as a 401 it can act on
        mockMvc.perform(get("/api/customers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidCustomerPostIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"Roe\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void browserRequestForProtectedPageGetsLoginPage() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/login"));
    }

    /*
    * owner.html logs out through a plain link because static html cannot carry a
    * csrf token. That link only works while spring generates the logout page, so a
    * custom login page added later must not quietly drop this filter.
    * */
    @Test
    void generatedLogoutPageIsAvailableForTheOwnerPanelLink() {
        boolean present = chain.getFilters().stream()
                .anyMatch(filter -> filter instanceof DefaultLogoutPageGeneratingFilter);
        org.junit.jupiter.api.Assertions.assertTrue(present, "owner.html logout link needs the generated logout page");
    }
    @Test
    void failedLoginTriggersSecurityLogging() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "test_admin")
                        .param("password", "wrong_password")
                        .with(csrf()))
                .andExpect(status().isFound());

        verify(securityEventLogger)
                .logFailedLogin(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }
    @Test
    void csrfFailureTriggersSecurityLogging() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());

        verify(securityEventLogger)
                .logUnauthorizedAccess(
                        org.mockito.ArgumentMatchers.any()
                );
    }
}
