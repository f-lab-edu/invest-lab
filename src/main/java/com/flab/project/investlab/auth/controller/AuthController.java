package com.flab.project.investlab.auth.controller;

import com.flab.project.investlab.auth.cookie.RefreshCookieManager;
import com.flab.project.investlab.auth.dto.LoginRequest;
import com.flab.project.investlab.auth.dto.TokenResponse;
import com.flab.project.investlab.auth.service.AuthService;
import com.flab.project.investlab.auth.service.LoginResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        LoginResult result = authService.login(request.email(), request.password());
        TokenResponse response = TokenResponse.bearer(
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
}
