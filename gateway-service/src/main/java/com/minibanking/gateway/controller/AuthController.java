package com.minibanking.gateway.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.springframework.security.oauth2.jwt.JwsHeader;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder) {

        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(
            @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mini-banking-gateway")
                .issuedAt(now)
                .expiresAt(
                        now.plus(
                                1,
                                ChronoUnit.HOURS
                        )
                )
                .subject(authentication.getName())
                .claim("role", "USER")
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();

        return new TokenResponse(
                token,
                "Bearer",
                3600
        );
    }

    public record LoginRequest(
            String username,
            String password
    ) {
    }

    public record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn
    ) {
    }
}
