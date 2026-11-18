package cscc.edu.feedback;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventLogger {

    private static final Logger log =
            LoggerFactory.getLogger(SecurityEventLogger.class);

    @EventListener
    public void onAuthenticationFailure(
            AbstractAuthenticationFailureEvent event) {

        String ip = "unknown";

        Object details = event.getAuthentication().getDetails();

        if (details instanceof WebAuthenticationDetails webDetails) {
            ip = webDetails.getRemoteAddress();
        }

        log.warn("SECURITY EVENT: Failed authentication; sourceIp={}; reason={}",
                ip,
                event.getException().getClass().getSimpleName());
    }

    public void logUnauthorizedAccess(HttpServletRequest request) {
        log.warn("SECURITY EVENT: Unauthorized access; method={}; path={}",
                request.getMethod(),
                request.getRequestURI());
    }
}


