package cscc.edu.feedback.config;

import cscc.edu.feedback.SecurityEventLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

/*
* Without this bean Spring Boot secures every request, which would put the public
* feedback form and its css/js behind a login. Customers stay anonymous, the owner
* panel and the reads it depends on require a login.
* */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final SecurityEventLogger securityEventLogger;

    public SecurityConfig(SecurityEventLogger securityEventLogger) {
        this.securityEventLogger = securityEventLogger;
    }
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests -> requests
                        //the public feedback form and everything it loads
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        //customers submit without an account
                        .requestMatchers(HttpMethod.POST, "/api/customers", "/api/feedback").permitAll()
                        //keeps a validation 400 from becoming a login redirect for anonymous callers
                        .requestMatchers("/error").permitAll()
                        //owner panel pages plus every GET on the api
                        .anyRequest().authenticated())
                /*
                * The two public POSTs are anonymous, so there are no credentials for a
                * forged request to ride on and nothing for a CSRF token to protect. Scoped
                * to POST so panel actions added later keep their protection.
                * */
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        pathPattern(HttpMethod.POST, "/api/customers"),
                        pathPattern(HttpMethod.POST, "/api/feedback")))
                .formLogin(form -> form
                        .failureHandler((request, response, exception) -> {
                            securityEventLogger.logFailedLogin(request, exception);
                            response.sendRedirect("/login?error");
                        })
                )
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, exception) -> {
                            securityEventLogger.logUnauthorizedAccess(request);
                            response.sendError(403, "Forbidden");
                        })
                )
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self' https://cdn.jsdelivr.net; " +
                                                "style-src 'self' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                                                "font-src 'self' https://fonts.gstatic.com; " +
                                                "img-src 'self' data:; " +
                                                "object-src 'none'; " +
                                                "base-uri 'self'; " +
                                                "frame-ancestors 'none'"
                                )
                        )
                )
                .build();
    }
}
