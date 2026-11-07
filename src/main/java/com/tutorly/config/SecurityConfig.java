package com.tutorly.config;

import com.tutorly.auth.AuthStudentRepository;
import com.tutorly.repository.TeacherRepository;
import jakarta.servlet.DispatcherType;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthStudentRepository students, TeacherRepository teachers) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(s -> s.securityContextRepository(new NullSecurityContextRepository()))
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login",
                                "/api/teacher-auth/register", "/api/teacher-auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/students").denyAll()
                        .requestMatchers("/api/teachers/me", "/api/teachers/me/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/teachers", "/api/teachers/*").hasAnyRole("STUDENT", "TEACHER")
                        // Every existing learning/profile endpoint remains student-only.
                        .anyRequest().hasRole("STUDENT"))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwt -> {
                    Long id = Long.valueOf(jwt.getSubject());
                    Object accountType = jwt.getClaims().get("accountType");
                    final String role;
                    // Previously issued student tokens have no accountType claim.
                    if (accountType == null || "STUDENT".equals(accountType)) {
                        if (!students.existsById(id)) throw invalidAccount();
                        role = "STUDENT";
                    } else if ("TEACHER".equals(accountType)) {
                        if (!teachers.existsById(id)) throw invalidAccount();
                        role = "TEACHER";
                    } else {
                        throw invalidAccount();
                    }
                    return new JwtAuthenticationToken(jwt,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)), id.toString());
                })));
        return http.build();
    }
    private OAuth2AuthenticationException invalidAccount() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Account unavailable or invalid account type", null));
    }
}
