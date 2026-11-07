package com.tutorly.auth;

import com.tutorly.config.JwtConfig;
import com.tutorly.dto.TeacherDtos.*;
import com.tutorly.model.Teacher;
import com.tutorly.repository.TeacherRepository;
import com.tutorly.service.TeacherService;
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
public class TeacherAuthService {
    private final TeacherRepository teachers;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final String dummyHash;
    public TeacherAuthService(TeacherRepository teachers, PasswordEncoder passwords, JwtEncoder encoder) {
        this.teachers=teachers;this.passwords=passwords;this.encoder=encoder;
        this.dummyHash=passwords.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public OwnProfile register(Register r) {
        if(r.password().getBytes(StandardCharsets.UTF_8).length>72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password must be at most 72 UTF-8 bytes");
        String email=r.email().strip().toLowerCase(Locale.ROOT);
        if(teachers.existsByEmailIgnoreCase(email))throw new ResponseStatusException(HttpStatus.CONFLICT,"Teacher email already registered");
        Teacher teacher=new Teacher(email,passwords.encode(r.password()));
        TeacherService.applyProfile(teacher,r.profile());
        // New profiles remain hidden until the teacher explicitly publishes them.
        teacher.setListed(false);
        try { return OwnProfile.from(teachers.saveAndFlush(teacher)); }
        catch(DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Teacher could not be created: conflicting data");
        }
    }

    @Transactional(readOnly=true)
    public Token login(Login r) {
        if(r.password().getBytes(StandardCharsets.UTF_8).length>72)throw invalidCredentials();
        Teacher t=teachers.findByEmailIgnoreCase(r.email().strip()).orElse(null);
        boolean matches=passwords.matches(r.password(),t==null?dummyHash:t.getPasswordHash());
        if(t==null || !matches)throw invalidCredentials();
        Instant now=Instant.now();
        var claims=JwtClaimsSet.builder().issuer(JwtConfig.ISSUER).audience(List.of(JwtConfig.AUDIENCE))
                .subject(t.getId().toString()).claim("accountType","TEACHER")
                .issuedAt(now).notBefore(now).expiresAt(now.plusSeconds(900)).id(UUID.randomUUID().toString()).build();
        String jwt=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(),claims)).getTokenValue();
        return new Token(jwt,"Bearer",900,t.getId());
    }
    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
    }
}
