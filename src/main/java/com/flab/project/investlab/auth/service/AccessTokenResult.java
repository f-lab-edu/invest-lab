package com.flab.project.investlab.auth.service;

public record AccessTokenResult(
        String accessToken,
        long accessTokenExpiresIn
) {
}
