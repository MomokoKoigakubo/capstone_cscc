package cscc.edu.feedback.config;

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
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .build();
    }
}
