package com.ragapp.auth;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ragapp.auth.OrgAccountService.OrgAccount;
import com.ragapp.dto.IndividualSessionResponse;
import com.ragapp.dto.LoginRequest;
import com.ragapp.dto.LoginResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final OrgAccountService orgAccountService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService,
                          OrgAccountService orgAccountService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.orgAccountService = orgAccountService;
    }

    /**
     * Organization login. Validates credentials and returns a JWT scoped to the
     * caller's organization ({@code org:<orgId>}) — all members of the same org
     * share one knowledge base.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .orElse("ORG_MEMBER");

        OrgAccount org = orgAccountService.findByUsername(request.username())
                .orElse(new OrgAccount(request.username(), request.username(), request.username(), role));

        String scopeKey = "org:" + org.orgId();
        String token = jwtService.generateToken(request.username(), scopeKey, role, org.orgName());

        return ResponseEntity.ok(
                new LoginResponse(token, request.username(), "ORGANIZATION", org.orgId(), org.orgName(), role)
        );
    }

    /**
     * Starts an anonymous individual session. No credentials required — mints a
     * JWT scoped to a fresh private workspace ({@code ind:<workspaceId>}).
     */
    @PostMapping("/individual-session")
    public ResponseEntity<IndividualSessionResponse> individualSession() {
        String workspaceId = UUID.randomUUID().toString();
        String scopeKey = "ind:" + workspaceId;
        String subject = "guest-" + workspaceId.substring(0, 8);

        String token = jwtService.generateToken(subject, scopeKey, "INDIVIDUAL", "Individual workspace");

        return ResponseEntity.ok(new IndividualSessionResponse(token, workspaceId, "INDIVIDUAL"));
    }
}
