package com.tutorly.auth;

import com.tutorly.config.JwtConfig;
import com.tutorly.dto.JwtAuthDtos.*;
import com.tutorly.model.Student;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JwtAuthService {
    private final AuthStudentRepository students;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final String dummyHash;
    public static final long TOKEN_SECONDS = 900;

    public JwtAuthService(AuthStudentRepository students, PasswordEncoder passwords, JwtEncoder encoder) {
        this.students = students;
        this.passwords = passwords;
        this.encoder = encoder;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public Profile register(Register request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (students.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        Student student = new Student();
        student.setName(request.name().strip());
        student.setEmail(email);
        student.setGrade(request.grade().strip());
        student.setCity(request.city().strip());
        student.setPasswordHash(passwords.encode(request.password()));
        try { return Profile.from(students.saveAndFlush(student)); }
        catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student could not be created: conflicting data");
        }
    }

    @Transactional(readOnly=true)
    public Token login(Login request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) throw badCredentials();
        Student student = students.findByEmailIgnoreCase(request.email().strip()).orElse(null);
        String hash = student == null ? null : student.getPasswordHash();
        boolean matches = passwords.matches(request.password(), hash == null ? dummyHash : hash);
        if (student == null || hash == null || !matches) throw badCredentials();
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER).audience(List.of(JwtConfig.AUDIENCE))
                .subject(student.getId().toString()).issuedAt(now).notBefore(now)
                .expiresAt(now.plusSeconds(TOKEN_SECONDS)).id(UUID.randomUUID().toString()).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
        return new Token(token, "Bearer", TOKEN_SECONDS, student.getId());
    }

    @Transactional(readOnly=true)
    public Profile me(Long id) {
        return students.findById(id).map(Profile::from).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student no longer exists"));
    }

    private ResponseStatusException badCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
