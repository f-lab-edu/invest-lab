package com.flab.project.investlab.auth.service;

public record LoginResult(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn
) {
}
