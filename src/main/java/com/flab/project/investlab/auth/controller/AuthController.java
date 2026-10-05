package com.flab.project.investlab.auth.controller;

import com.flab.project.investlab.auth.cookie.RefreshCookieManager;
import com.flab.project.investlab.auth.dto.LoginRequest;
import com.flab.project.investlab.auth.dto.TokenResponse;
import com.flab.project.investlab.auth.service.AuthService;
import com.flab.project.investlab.auth.service.LoginResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshCookieManager refreshCookieManager;

    public AuthController(AuthService authService, RefreshCookieManager refreshCookieManager) {
        this.authService = authService;
        this.refreshCookieManager = refreshCookieManager;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        final LoginResult result = authService.login(request.email(), request.password());
        final TokenResponse response = TokenResponse.bearer(
                result.accessToken(),
                result.accessTokenExpiresIn()
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookieManager.create(result.refreshToken()).toString()
                )
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = RefreshCookieManager.COOKIE_NAME, required = false)
            String refreshToken
    ) {
        final LoginResult result = authService.refresh(refreshToken);
        final TokenResponse response = TokenResponse.bearer(
                result.accessToken(),
                result.accessTokenExpiresIn()
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookieManager.create(result.refreshToken()).toString()
                )
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshCookieManager.COOKIE_NAME, required = false)
            String refreshToken
    ) {
        authService.logout(refreshToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookieManager.expire().toString()
                )
                .build();
    }
}
