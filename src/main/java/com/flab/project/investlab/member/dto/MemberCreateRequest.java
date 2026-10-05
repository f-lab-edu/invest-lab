package com.flab.project.investlab.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 10, max = 64) String password,
        @NotBlank @Size(min = 2, max = 20) String nickname
) {

    public MemberCreateRequest {
        email = email == null ? null : email.trim();
        nickname = nickname == null ? null : nickname.trim();
    }
}
