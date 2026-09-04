package cm.afriland.enquete;

import cm.afriland.enquete.security.GoogleOAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public SecurityConfig(GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler) {
        this.googleOAuth2SuccessHandler = googleOAuth2SuccessHandler;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .securityContext(ctx -> ctx.securityContextRepository(securityContextRepository()))
            .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                (request, response, authException) -> response.sendError(401, "Session expirée. Veuillez vous reconnecter."),
                request -> request.getRequestURI().startsWith("/api/")
            ))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/error", "/api/health", "/api/auth/**", "/api/public/**", "/oauth2/**", "/login/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/support/conversations", "/api/support/conversations/*/messages", "/api/support/ai").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth.successHandler(googleOAuth2SuccessHandler))
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl(frontendUrl + "/")
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }
}
